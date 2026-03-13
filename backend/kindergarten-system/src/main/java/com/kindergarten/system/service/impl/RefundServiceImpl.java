/**
 * 退费计算服务实现类
 * <p>
 * 根据学生考勤记录计算应退金额，核心逻辑：
 * 1. 获取学生学期内的请假日期列表
 * 2. 识别连续请假区间（相邻日期差为1天）
 * 3. 根据请假天数匹配退费规则
 * 4. 计算各区间退费金额并汇总
 * </p>
 * <p>
 * 退费规则：
 * - 连续 5-9 天：仅退伙食费（12.7元/天）
 * - 连续 10天及以上：退保教费（66.3元/天）+ 伙食费（61.7元/天）
 * - 不足 5 天：不退费
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
package com.kindergarten.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kindergarten.system.common.exception.BusinessException;
import com.kindergarten.system.common.result.ResultCode;
import com.kindergarten.system.dto.RefundCalculateResult;
import com.kindergarten.system.dto.RefundCalculateResult.LeaveSegment;
import com.kindergarten.system.dto.RefundCalculateResult.RefundItem;
import com.kindergarten.system.entity.RefundRecord;
import com.kindergarten.system.entity.RefundRuleConfig;
import com.kindergarten.system.entity.Semester;
import com.kindergarten.system.entity.Student;
import com.kindergarten.system.mapper.AttendanceMapper;
import com.kindergarten.system.mapper.RefundRecordMapper;
import com.kindergarten.system.mapper.RefundRuleConfigMapper;
import com.kindergarten.system.mapper.SemesterMapper;
import com.kindergarten.system.mapper.StudentMapper;
import com.kindergarten.system.service.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    /** 学生数据访问 */
    private final StudentMapper studentMapper;

    /** 学期数据访问 */
    private final SemesterMapper semesterMapper;

    /** 考勤数据访问 */
    private final AttendanceMapper attendanceMapper;

    /** 退费规则数据访问 */
    private final RefundRuleConfigMapper refundRuleConfigMapper;

    /** 退费记录数据访问 */
    private final RefundRecordMapper refundRecordMapper;

    /**
     * 计算学生某学期的退费金额
     * <p>
     * 计算流程：
     * 1. 验证学生和学期是否存在
     * 2. 解析统计日期范围（未传则使用学期起止日期）
     * 3. 获取范围内的请假日期列表
     * 4. 识别连续请假区间
     * 5. 获取适用的退费规则
     * 6. 计算各区间退费金额
     * 7. 汇总返回结果
     * </p>
     *
     * @param studentId 学生ID
     * @param semesterId 学期ID
     * @param startDate 统计开始日期（可选）
     * @param endDate 统计结束日期（可选）
     * @return 退费计算结果，包含连续请假区间明细和总退费金额
     * @throws BusinessException 学生或学期不存在时抛出
     */
    @Override
    public RefundCalculateResult calculateRefund(Long studentId, Long semesterId, String startDate, String endDate) {
        // 1. 验证学生和学期
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        Semester semester = semesterMapper.selectById(semesterId);
        if (semester == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        // 2. 获取统计范围内的请假日期列表
        LocalDate rangeStart = resolveStartDate(semester, startDate);
        LocalDate rangeEnd = resolveEndDate(semester, endDate);

        if (rangeStart.isAfter(rangeEnd)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        List<String> leaveDateStrs = attendanceMapper.selectLeaveDates(
                studentId,
                rangeStart.toString(),
                rangeEnd.toString()
        );

        if (leaveDateStrs == null || leaveDateStrs.isEmpty()) {
            return buildEmptyResult(student, semester, rangeStart, rangeEnd);
        }

        // 3. 将日期字符串转为 LocalDate 并排序
        List<LocalDate> leaveDates = leaveDateStrs.stream()
                .map(LocalDate::parse)
                .sorted()
                .collect(Collectors.toList());

        // 4. 识别连续请假区间
        List<LeaveSegment> segments = identifyLeaveSegments(leaveDates);

        // 4.1 计算请假总天数（按日期范围内的请假记录统计）
        int totalLeaveDays = leaveDates.size();

        // 5. 获取退费规则
        List<RefundRuleConfig> rules = getRefundRules(semesterId);
        if (rules == null || rules.isEmpty()) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        // 6. 计算每个区间的退费金额
        BigDecimal totalRefund = BigDecimal.ZERO;
        for (LeaveSegment segment : segments) {
            calculateSegmentRefund(segment, rules);
            totalRefund = totalRefund.add(segment.getSegmentAmount());
        }

        // 7. 构建返回结果
        RefundCalculateResult result = new RefundCalculateResult();
        result.setStudentId(studentId);
        result.setStudentName(student.getName());
        result.setSemesterId(semesterId);
        result.setSemesterName(semester.getSemesterName());
        result.setStartDate(rangeStart.toString());
        result.setEndDate(rangeEnd.toString());
        result.setLeaveSegments(segments);
        result.setTotalLeaveDays(totalLeaveDays);
        result.setTotalRefundAmount(totalRefund.setScale(2, RoundingMode.HALF_UP));

        return result;
    }

    /**
     * 识别连续请假区间
     * <p>
     * 算法逻辑：遍历排序后的日期列表，判断相邻日期是否连续（差值为1天）
     * 若不连续则分割为新的区间
     * </p>
     *
     * @param sortedDates 已排序的请假日期列表
     * @return 连续请假区间列表
     */
    private List<LeaveSegment> identifyLeaveSegments(List<LocalDate> sortedDates) {
        List<LeaveSegment> segments = new ArrayList<>();

        if (sortedDates.isEmpty()) {
            return segments;
        }

        LocalDate segmentStart = sortedDates.get(0);
        LocalDate segmentEnd = sortedDates.get(0);
        int segmentDays = 1;

        for (int i = 1; i < sortedDates.size(); i++) {
            LocalDate currentDate = sortedDates.get(i);

            if (isContinuous(segmentEnd, currentDate)) {
                // 连续日期（含周末间隔），延长当前区间
                segmentEnd = currentDate;
                segmentDays++;
            } else {
                // 不连续，保存当前区间，开始新区间
                segments.add(createSegment(segmentStart, segmentEnd, segmentDays));
                segmentStart = currentDate;
                segmentEnd = currentDate;
                segmentDays = 1;
            }
        }

        // 添加最后一个区间
        segments.add(createSegment(segmentStart, segmentEnd, segmentDays));

        return segments;
    }

    private LeaveSegment createSegment(LocalDate start, LocalDate end, int days) {
        LeaveSegment segment = new LeaveSegment();
        segment.setStartDate(start.toString());
        segment.setEndDate(end.toString());
        // 仅统计实际请假天数（不含周末）
        segment.setDays(days);
        return segment;
    }

    private boolean isContinuous(LocalDate previousDate, LocalDate currentDate) {
        long gapDays = ChronoUnit.DAYS.between(previousDate, currentDate);
        if (gapDays == 1) {
            return true;
        }

        if (gapDays > 1 && gapDays <= 3) {
            // 允许跨周末连续：两次请假日期之间只包含周六/周日
            for (LocalDate date = previousDate.plusDays(1); date.isBefore(currentDate); date = date.plusDays(1)) {
                DayOfWeek dayOfWeek = date.getDayOfWeek();
                if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    private LocalDate resolveStartDate(Semester semester, String startDate) {
        if (startDate == null || startDate.isBlank()) {
            return semester.getStartDate();
        }
        return LocalDate.parse(startDate);
    }

    private LocalDate resolveEndDate(Semester semester, String endDate) {
        if (endDate == null || endDate.isBlank()) {
            return semester.getEndDate();
        }
        return LocalDate.parse(endDate);
    }

    /**
     * 计算单个区间的退费金额
     * <p>
     * 根据区间天数匹配退费规则：
     * - 5-9天：匹配 LEAVE_5_9 规则
     * - 10天及以上：匹配 LEAVE_10_PLUS 规则
     * - 不足5天：不退费
     * </p>
     *
     * @param segment 请假区间
     * @param rules   退费规则列表
     */
    private void calculateSegmentRefund(LeaveSegment segment, List<RefundRuleConfig> rules) {
        int days = segment.getDays();
        List<RefundItem> refundItems = new ArrayList<>();
        BigDecimal segmentTotal = BigDecimal.ZERO;

        // 匹配规则：5-9天 -> LEAVE_5_9, 10天+ -> LEAVE_10_PLUS
        String ruleType;
        if (days >= 5 && days <= 9) {
            ruleType = "LEAVE_5_9";
        } else if (days >= 10) {
            ruleType = "LEAVE_10_PLUS";
        } else {
            // 不足5天，不退费
            segment.setMatchedRuleType("NONE");
            segment.setRefundItems(refundItems);
            segment.setSegmentAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            return;
        }

        segment.setMatchedRuleType(ruleType);

        // 查找匹配的规则
        List<RefundRuleConfig> matchedRules = rules.stream()
                .filter(r -> ruleType.equals(r.getRuleType()) && r.getStatus() != null && r.getStatus() == 1)
                .collect(Collectors.toList());

        if (matchedRules.isEmpty()) {
            segment.setRefundItems(refundItems);
            segment.setSegmentAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            return;
        }

        for (RefundRuleConfig rule : matchedRules) {
            BigDecimal dailyRate = rule.getDailyRate();
            BigDecimal amount = dailyRate.multiply(BigDecimal.valueOf(days))
                    .setScale(2, RoundingMode.HALF_UP);

            RefundItem item = new RefundItem();
            item.setFeeTypeCode(rule.getFeeTypeCode());
            item.setFeeTypeName(getFeeTypeName(rule.getFeeTypeCode()));
            item.setDailyRate(dailyRate);
            item.setAmount(amount);

            refundItems.add(item);
            segmentTotal = segmentTotal.add(amount);
        }

        segment.setRefundItems(refundItems);
        segment.setSegmentAmount(segmentTotal);
    }

    /**
     * 获取费用类型名称
     */
    private String getFeeTypeName(String feeTypeCode) {
        return switch (feeTypeCode) {
            case "education" -> "保教费";
            case "meal" -> "伙食费";
            default -> feeTypeCode;
        };
    }

    /**
     * 获取退费规则
     * <p>
     * 优先查询学期特定规则，若不存在则回退到全局规则
     * </p>
     *
     * @param semesterId 学期ID
     * @return 适用的退费规则列表
     */
    private List<RefundRuleConfig> getRefundRules(Long semesterId) {
        // 先查询学期特定规则
        List<RefundRuleConfig> semesterRules = refundRuleConfigMapper.selectList(
                new LambdaQueryWrapper<RefundRuleConfig>()
                        .eq(RefundRuleConfig::getSemesterId, semesterId)
                        .eq(RefundRuleConfig::getStatus, 1)
        );

        if (!semesterRules.isEmpty()) {
            return semesterRules;
        }

        // 回退到全局规则
        return refundRuleConfigMapper.selectList(
                new LambdaQueryWrapper<RefundRuleConfig>()
                        .isNull(RefundRuleConfig::getSemesterId)
                        .eq(RefundRuleConfig::getStatus, 1)
        );
    }

    /**
     * 构建空结果（无请假记录）
     */
    private RefundCalculateResult buildEmptyResult(Student student, Semester semester, LocalDate rangeStart, LocalDate rangeEnd) {
        RefundCalculateResult result = new RefundCalculateResult();
        result.setStudentId(student.getId());
        result.setStudentName(student.getName());
        result.setSemesterId(semester.getId());
        result.setSemesterName(semester.getSemesterName());
        result.setStartDate(rangeStart.toString());
        result.setEndDate(rangeEnd.toString());
        result.setLeaveSegments(new ArrayList<>());
        result.setTotalLeaveDays(0);
        result.setTotalRefundAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        return result;
    }

    /**
     * 计算并保存退费记录
     * <p>
     * 计算退费金额后，将结果保存到 refund_record 表
     * </p>
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @return 退费计算结果
     */
    @Override
    public RefundCalculateResult calculateAndSaveRefund(Long studentId, Long semesterId) {
        // 先计算退费
        RefundCalculateResult result = calculateRefund(studentId, semesterId, null, null);

        // 查找是否已存在记录
        RefundRecord existingRecord = refundRecordMapper.selectOne(
                new LambdaQueryWrapper<RefundRecord>()
                        .eq(RefundRecord::getStudentId, studentId)
                        .eq(RefundRecord::getSemesterId, semesterId)
                        .eq(RefundRecord::getRefundType, "LEAVE")
        );

        // 将退费明细序列化为 JSON
        String detailJson = serializeRefundDetails(result.getLeaveSegments());

        if (existingRecord != null) {
            // 更新现有记录
            existingRecord.setLeaveDaysTotal(result.getTotalLeaveDays());
            existingRecord.setRefundAmount(result.getTotalRefundAmount());
            existingRecord.setDetailJson(detailJson);
            existingRecord.setCalculatedAt(java.time.LocalDateTime.now());
            refundRecordMapper.updateById(existingRecord);
        } else {
            // 创建新记录
            RefundRecord newRecord = new RefundRecord();
            newRecord.setStudentId(studentId);
            newRecord.setSemesterId(semesterId);
            newRecord.setLeaveDaysTotal(result.getTotalLeaveDays());
            newRecord.setRefundAmount(result.getTotalRefundAmount());
            newRecord.setDetailJson(detailJson);
            newRecord.setCalculatedAt(java.time.LocalDateTime.now());
            refundRecordMapper.insert(newRecord);
        }

        return result;
    }

    /**
     * 获取已保存的退费记录
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @return 退费计算结果（从数据库读取）
     */
    @Override
    public RefundCalculateResult getSavedRefund(Long studentId, Long semesterId) {
        Student student = studentMapper.selectById(studentId);
        Semester semester = semesterMapper.selectById(semesterId);

        RefundRecord record = refundRecordMapper.selectOne(
                new LambdaQueryWrapper<RefundRecord>()
                        .eq(RefundRecord::getStudentId, studentId)
                        .eq(RefundRecord::getSemesterId, semesterId)
                        .eq(RefundRecord::getRefundType, "LEAVE")
        );

        if (record == null) {
            // 没有保存的记录，返回空结果
            if (student != null && semester != null) {
                return buildEmptyResult(student, semester, semester.getStartDate(), semester.getEndDate());
            }
            return null;
        }

        // 从数据库记录构建返回结果
        RefundCalculateResult result = new RefundCalculateResult();
        result.setStudentId(studentId);
        result.setStudentName(student != null ? student.getName() : null);
        result.setSemesterId(semesterId);
        result.setSemesterName(semester != null ? semester.getSemesterName() : null);
        result.setStartDate(semester != null ? semester.getStartDate().toString() : null);
        result.setEndDate(semester != null ? semester.getEndDate().toString() : null);
        result.setTotalLeaveDays(record.getLeaveDaysTotal());
        result.setTotalRefundAmount(record.getRefundAmount());

        // 反序列化明细 JSON
        if (record.getDetailJson() != null && !record.getDetailJson().isEmpty()) {
            result.setLeaveSegments(deserializeRefundDetails(record.getDetailJson()));
        } else {
            result.setLeaveSegments(new ArrayList<>());
        }

        return result;
    }

    /**
     * 序列化退费明细为 JSON
     */
    private String serializeRefundDetails(List<LeaveSegment> segments) {
        if (segments == null || segments.isEmpty()) {
            return "[]";
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(segments);
        } catch (Exception e) {
            log.error("序列化退费明细失败", e);
            return "[]";
        }
    }

    /**
     * 反序列化退费明细
     */
    private List<LeaveSegment> deserializeRefundDetails(String json) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.core.type.TypeReference<List<LeaveSegment>> typeRef =
                    new com.fasterxml.jackson.core.type.TypeReference<>() {};
            return mapper.readValue(json, typeRef);
        } catch (Exception e) {
            log.error("反序列化退费明细失败", e);
            return new ArrayList<>();
        }
    }

    // ==================== 离园退费相关方法 ====================

    /**
     * 计算学生学期中途离园退费金额
     * <p>
     * 当学生在学期中途离园时，根据实际在园天数计算退费金额。
     * 计算逻辑：
     * 1. 获取学生离园日期（student.leave_date）
     * 2. 计算学期开始到离园日期之间的有效工作日（实际在园天数）
     * 3. 计算离园日期到学期结束之间的有效工作日（应退天数）
     * 4. 按日均费用计算退费金额
     * </p>
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @return 离园退费计算结果
     */
    @Override
    public RefundCalculateResult calculateDropoutRefund(Long studentId, Long semesterId) {
        // 1. 验证学生和学期
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        // 验证学生是否已离园
        if (student.getLeaveDate() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "学生未离园，无法计算离园退费");
        }

        Semester semester = semesterMapper.selectById(semesterId);
        if (semester == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        LocalDate leaveDate = student.getLeaveDate();
        LocalDate semesterStart = semester.getStartDate();
        LocalDate semesterEnd = semester.getEndDate();

        // 2. 计算实际在园天数和应退天数
        int actualAttendDays;
        int refundDays;
        
        // 如果离园日期在学期开始之前，说明学生本学期没有就读，应退全部费用
        if (leaveDate.isBefore(semesterStart)) {
            actualAttendDays = 0;
            refundDays = calculateWorkDays(semesterStart, semesterEnd);
        }
        // 如果离园日期在学期结束之后（理论上不应发生），按全学期计算
        else if (leaveDate.isAfter(semesterEnd)) {
            actualAttendDays = calculateWorkDays(semesterStart, semesterEnd);
            refundDays = 0;
        }
        // 离园日期在学期内，正常计算
        else {
            actualAttendDays = calculateWorkDays(semesterStart, leaveDate.minusDays(1));
            refundDays = calculateWorkDays(leaveDate, semesterEnd);
        }

        // 4. 计算学期总工作日
        int totalWorkDays = calculateWorkDays(semesterStart, semesterEnd);

        // 5. 获取退费规则（使用 LEAVE_10_PLUS 规则的日均费率作为离园退费标准）
        List<RefundRuleConfig> rules = getDropoutRefundRules(semesterId);

        // 6. 计算退费金额
        List<RefundItem> refundItems = new ArrayList<>();
        BigDecimal totalRefund = BigDecimal.ZERO;

        for (RefundRuleConfig rule : rules) {
            BigDecimal dailyRate = rule.getDailyRate();
            BigDecimal amount = dailyRate.multiply(BigDecimal.valueOf(refundDays))
                    .setScale(2, RoundingMode.HALF_UP);

            RefundItem item = new RefundItem();
            item.setFeeTypeCode(rule.getFeeTypeCode());
            item.setFeeTypeName(getFeeTypeName(rule.getFeeTypeCode()));
            item.setDailyRate(dailyRate);
            item.setAmount(amount);
            refundItems.add(item);

            totalRefund = totalRefund.add(amount);
        }

        // 7. 构建返回结果
        RefundCalculateResult result = new RefundCalculateResult();
        result.setStudentId(studentId);
        result.setStudentName(student.getName());
        result.setSemesterId(semesterId);
        result.setSemesterName(semester.getSemesterName());
        result.setStartDate(semesterStart.toString());
        result.setEndDate(semesterEnd.toString());
        result.setTotalLeaveDays(refundDays); // 复用字段存储应退天数
        result.setTotalRefundAmount(totalRefund.setScale(2, RoundingMode.HALF_UP));

        // 构建单个请假区间表示离园退费
        LeaveSegment segment = new LeaveSegment();
        segment.setStartDate(leaveDate.toString());
        segment.setEndDate(semesterEnd.toString());
        segment.setDays(refundDays);
        segment.setMatchedRuleType("DROPOUT");
        segment.setRefundItems(refundItems);
        segment.setSegmentAmount(totalRefund.setScale(2, RoundingMode.HALF_UP));

        result.setLeaveSegments(List.of(segment));

        return result;
    }

    /**
     * 计算并保存离园退费记录
     */
    @Override
    public RefundCalculateResult calculateAndSaveDropoutRefund(Long studentId, Long semesterId) {
        RefundCalculateResult result = calculateDropoutRefund(studentId, semesterId);

        // 查找是否已存在离园退费记录
        RefundRecord existingRecord = refundRecordMapper.selectOne(
                new LambdaQueryWrapper<RefundRecord>()
                        .eq(RefundRecord::getStudentId, studentId)
                        .eq(RefundRecord::getSemesterId, semesterId)
                        .eq(RefundRecord::getRefundType, "LEAVE")
                        .eq(RefundRecord::getRefundType, "DROPOUT")
        );

        Student student = studentMapper.selectById(studentId);
        String detailJson = serializeRefundDetails(result.getLeaveSegments());

        if (existingRecord != null) {
            existingRecord.setLeaveDaysTotal(null);
            existingRecord.setActualAttendDays(calculateWorkDays(
                    semesterMapper.selectById(semesterId).getStartDate(),
                    student.getLeaveDate().minusDays(1)));
            existingRecord.setRefundDays(result.getTotalLeaveDays());
            existingRecord.setLeaveDate(student.getLeaveDate());
            existingRecord.setRefundAmount(result.getTotalRefundAmount());
            existingRecord.setDetailJson(detailJson);
            existingRecord.setCalculatedAt(java.time.LocalDateTime.now());
            refundRecordMapper.updateById(existingRecord);
        } else {
            RefundRecord newRecord = new RefundRecord();
            newRecord.setStudentId(studentId);
            newRecord.setSemesterId(semesterId);
            newRecord.setRefundType("DROPOUT");
            newRecord.setActualAttendDays(calculateWorkDays(
                    semesterMapper.selectById(semesterId).getStartDate(),
                    student.getLeaveDate().minusDays(1)));
            newRecord.setRefundDays(result.getTotalLeaveDays());
            newRecord.setLeaveDate(student.getLeaveDate());
            newRecord.setRefundAmount(result.getTotalRefundAmount());
            newRecord.setDetailJson(detailJson);
            newRecord.setCalculatedAt(java.time.LocalDateTime.now());
            refundRecordMapper.insert(newRecord);
        }

        return result;
    }

    /**
     * 获取已保存的离园退费记录
     */
    @Override
    public RefundCalculateResult getSavedDropoutRefund(Long studentId, Long semesterId) {
        Student student = studentMapper.selectById(studentId);
        Semester semester = semesterMapper.selectById(semesterId);

        RefundRecord record = refundRecordMapper.selectOne(
                new LambdaQueryWrapper<RefundRecord>()
                        .eq(RefundRecord::getStudentId, studentId)
                        .eq(RefundRecord::getSemesterId, semesterId)
                        .eq(RefundRecord::getRefundType, "DROPOUT")
        );

        if (record == null) {
            return null;
        }

        RefundCalculateResult result = new RefundCalculateResult();
        result.setStudentId(studentId);
        result.setStudentName(student != null ? student.getName() : null);
        result.setSemesterId(semesterId);
        result.setSemesterName(semester != null ? semester.getSemesterName() : null);
        result.setStartDate(semester != null ? semester.getStartDate().toString() : null);
        result.setEndDate(semester != null ? semester.getEndDate().toString() : null);
        result.setTotalLeaveDays(record.getRefundDays());
        result.setTotalRefundAmount(record.getRefundAmount());

        if (record.getDetailJson() != null && !record.getDetailJson().isEmpty()) {
            result.setLeaveSegments(deserializeRefundDetails(record.getDetailJson()));
        } else {
            result.setLeaveSegments(new ArrayList<>());
        }

        return result;
    }

    /**
     * 计算两个日期之间的工作日天数（不含周末）
     *
     * @param start 开始日期
     * @param end   结束日期
     * @return 工作日天数
     */
    private int calculateWorkDays(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            return 0;
        }

        int workDays = 0;
        LocalDate current = start;
        while (!current.isAfter(end)) {
            DayOfWeek dayOfWeek = current.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                workDays++;
            }
            current = current.plusDays(1);
        }
        return workDays;
    }

    /**
     * 获取离园退费规则
     * <p>
     * 离园退费使用 LEAVE_10_PLUS 规则的日均费率
     * </p>
     */
    private List<RefundRuleConfig> getDropoutRefundRules(Long semesterId) {
        // 先查询学期特定规则
        List<RefundRuleConfig> semesterRules = refundRuleConfigMapper.selectList(
                new LambdaQueryWrapper<RefundRuleConfig>()
                        .eq(RefundRuleConfig::getSemesterId, semesterId)
                        .eq(RefundRuleConfig::getRuleType, "LEAVE_10_PLUS")
                        .eq(RefundRuleConfig::getStatus, 1)
        );

        if (!semesterRules.isEmpty()) {
            return semesterRules;
        }

        // 回退到全局规则
        return refundRuleConfigMapper.selectList(
                new LambdaQueryWrapper<RefundRuleConfig>()
                        .isNull(RefundRuleConfig::getSemesterId)
                        .eq(RefundRuleConfig::getRuleType, "LEAVE_10_PLUS")
                        .eq(RefundRuleConfig::getStatus, 1)
        );
    }

}
