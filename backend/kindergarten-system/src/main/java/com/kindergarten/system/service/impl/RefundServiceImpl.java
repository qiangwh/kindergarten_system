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
import com.kindergarten.system.entity.RefundRuleConfig;
import com.kindergarten.system.entity.Semester;
import com.kindergarten.system.entity.Student;
import com.kindergarten.system.mapper.AttendanceMapper;
import com.kindergarten.system.mapper.RefundRuleConfigMapper;
import com.kindergarten.system.mapper.SemesterMapper;
import com.kindergarten.system.mapper.StudentMapper;
import com.kindergarten.system.service.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    /**
     * 计算学生某学期的退费金额
     * <p>
     * 计算流程：
     * 1. 验证学生和学期是否存在
     * 2. 获取学期内的请假日期列表
     * 3. 识别连续请假区间
     * 4. 获取适用的退费规则
     * 5. 计算各区间退费金额
     * 6. 汇总返回结果
     * </p>
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @return 退费计算结果，包含连续请假区间明细和总退费金额
     * @throws BusinessException 学生或学期不存在时抛出
     */
    @Override
    public RefundCalculateResult calculateRefund(Long studentId, Long semesterId) {
        // 1. 验证学生和学期
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        Semester semester = semesterMapper.selectById(semesterId);
        if (semester == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        // 2. 获取学期内的请假日期列表
        List<String> leaveDateStrs = attendanceMapper.selectLeaveDates(
                studentId,
                semester.getStartDate().toString(),
                semester.getEndDate().toString()
        );

        if (leaveDateStrs == null || leaveDateStrs.isEmpty()) {
            return buildEmptyResult(student, semester);
        }

        // 3. 将日期字符串转为 LocalDate 并排序
        List<LocalDate> leaveDates = leaveDateStrs.stream()
                .map(LocalDate::parse)
                .sorted()
                .collect(Collectors.toList());

        // 4. 识别连续请假区间
        List<LeaveSegment> segments = identifyLeaveSegments(leaveDates);

        // 5. 获取退费规则
        List<RefundRuleConfig> rules = getRefundRules(semesterId);

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
        result.setLeaveSegments(segments);
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

        for (int i = 1; i < sortedDates.size(); i++) {
            LocalDate currentDate = sortedDates.get(i);
            LocalDate expectedDate = segmentEnd.plusDays(1);

            if (currentDate.equals(expectedDate)) {
                // 连续日期，延长当前区间
                segmentEnd = currentDate;
            } else {
                // 不连续，保存当前区间，开始新区间
                segments.add(createSegment(segmentStart, segmentEnd));
                segmentStart = currentDate;
                segmentEnd = currentDate;
            }
        }

        // 添加最后一个区间
        segments.add(createSegment(segmentStart, segmentEnd));

        return segments;
    }

    private LeaveSegment createSegment(LocalDate start, LocalDate end) {
        LeaveSegment segment = new LeaveSegment();
        segment.setStartDate(start.toString());
        segment.setEndDate(end.toString());
        segment.setDays((int) ChronoUnit.DAYS.between(start, end) + 1);
        return segment;
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
                .filter(r -> ruleType.equals(r.getRuleType()) && r.getStatus() == 1)
                .collect(Collectors.toList());

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
    private RefundCalculateResult buildEmptyResult(Student student, Semester semester) {
        RefundCalculateResult result = new RefundCalculateResult();
        result.setStudentId(student.getId());
        result.setStudentName(student.getName());
        result.setSemesterId(semester.getId());
        result.setSemesterName(semester.getSemesterName());
        result.setLeaveSegments(new ArrayList<>());
        result.setTotalRefundAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        return result;
    }

}
