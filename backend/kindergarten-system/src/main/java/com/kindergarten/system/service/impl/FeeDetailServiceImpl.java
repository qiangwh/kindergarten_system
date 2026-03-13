package com.kindergarten.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kindergarten.system.dto.FeeDetailQuery;
import com.kindergarten.system.dto.RefundCalculateResult;
import com.kindergarten.system.dto.RefundCalculateResult.LeaveSegment;
import com.kindergarten.system.dto.RefundCalculateResult.RefundItem;
import com.kindergarten.system.dto.StudentFeeDetail;
import com.kindergarten.system.entity.*;
import com.kindergarten.system.mapper.*;
import com.kindergarten.system.service.FeeDetailService;
import com.kindergarten.system.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 收费明细服务实现类
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class FeeDetailServiceImpl implements FeeDetailService {

    private final StudentMapper studentMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final AttendanceMapper attendanceMapper;
    private final FeeTypeMapper feeTypeMapper;
    private final SemesterMapper semesterMapper;
    private final ClassInfoMapper classInfoMapper;
    private final RefundService refundService;

    @Override
    public IPage<StudentFeeDetail> pageStudentFeeDetails(FeeDetailQuery query) {
        // 构建学生查询条件
        LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(Student::getStatus, "active");
        
        if (query.getClassId() != null) {
            studentWrapper.eq(Student::getClassId, query.getClassId());
        }
        if (query.getStudentName() != null && !query.getStudentName().isEmpty()) {
            studentWrapper.like(Student::getName, query.getStudentName());
        }
        studentWrapper.orderByAsc(Student::getClassId).orderByAsc(Student::getId);

        // 分页查询学生
        Page<Student> studentPage = new Page<>(query.getPage(), query.getPageSize());
        IPage<Student> students = studentMapper.selectPage(studentPage, studentWrapper);

        // 转换为收费明细
        Page<StudentFeeDetail> resultPage = new Page<>(students.getCurrent(), students.getSize(), students.getTotal());
        List<StudentFeeDetail> details = students.getRecords().stream()
                .map(student -> buildStudentFeeDetail(student, query.getSemesterId()))
                .collect(Collectors.toList());
        resultPage.setRecords(details);

        return resultPage;
    }

    @Override
    public StudentFeeDetail getStudentFeeDetail(Long studentId, Long semesterId) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            return null;
        }
        return buildStudentFeeDetail(student, semesterId);
    }

    @Override
    public List<ClassFeeSummary> getClassFeeSummary(Long semesterId) {
        // 获取所有班级
        List<ClassInfo> classes = classInfoMapper.selectList(
                new LambdaQueryWrapper<ClassInfo>().eq(ClassInfo::getStatus, 1));

        List<ClassFeeSummary> summaries = new ArrayList<>();
        for (ClassInfo classInfo : classes) {
            // 获取班级学生
            List<Student> students = studentMapper.selectList(
                    new LambdaQueryWrapper<Student>()
                            .eq(Student::getClassId, classInfo.getId())
                            .eq(Student::getStatus, "active"));

            BigDecimal totalPaid = BigDecimal.ZERO;
            BigDecimal totalRefund = BigDecimal.ZERO;

            for (Student student : students) {
                // 缴费总额
                List<PaymentRecord> payments = paymentRecordMapper.selectList(
                        new LambdaQueryWrapper<PaymentRecord>()
                                .eq(PaymentRecord::getStudentId, student.getId())
                                .eq(PaymentRecord::getSemesterId, semesterId));
                BigDecimal paid = payments.stream()
                        .map(PaymentRecord::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                totalPaid = totalPaid.add(paid);

                // 退费总额（通过退费计算服务获取）
                try {
                    RefundCalculateResult refundResult = refundService.calculateRefund(
                            student.getId(), semesterId, null, null);
                    totalRefund = totalRefund.add(refundResult.getTotalRefundAmount());
                } catch (Exception e) {
                    // 退费计算失败时忽略，按0处理
                }
            }

            summaries.add(new ClassFeeSummary(
                    classInfo.getId(),
                    classInfo.getClassName(),
                    students.size(),
                    totalPaid,
                    totalRefund,
                    totalPaid.subtract(totalRefund)
            ));
        }

        return summaries;
    }

    /**
     * 构建学生收费明细
     */
    private StudentFeeDetail buildStudentFeeDetail(Student student, Long semesterId) {
        StudentFeeDetail detail = new StudentFeeDetail();
        detail.setStudentId(student.getId());
        detail.setStudentName(student.getName());
        detail.setClassId(student.getClassId());
        detail.setSemesterId(semesterId);

        // 获取班级名称
        ClassInfo classInfo = classInfoMapper.selectById(student.getClassId());
        if (classInfo != null) {
            detail.setClassName(classInfo.getClassName());
        }

        // 获取学期名称
        Semester semester = semesterMapper.selectById(semesterId);
        if (semester != null) {
            detail.setSemesterName(semester.getSemesterName());
        }

        // 获取缴费记录
        List<PaymentRecord> payments = paymentRecordMapper.selectList(
                new LambdaQueryWrapper<PaymentRecord>()
                        .eq(PaymentRecord::getStudentId, student.getId())
                        .eq(PaymentRecord::getSemesterId, semesterId));

        // 获取所有费用类型
        List<FeeType> feeTypes = feeTypeMapper.selectList(
                new LambdaQueryWrapper<FeeType>().eq(FeeType::getStatus, 1));
        Map<Long, FeeType> feeTypeMap = feeTypes.stream()
                .collect(Collectors.toMap(FeeType::getId, ft -> ft));

        // 按费用类型汇总缴费
        Map<Long, BigDecimal> paidByType = payments.stream()
                .collect(Collectors.groupingBy(
                        PaymentRecord::getFeeTypeId,
                        Collectors.reducing(BigDecimal.ZERO, PaymentRecord::getAmount, BigDecimal::add)
                ));

        // 调用退费服务获取退费金额和请假明细
        // 优先从数据库读取已保存的退费记录，如果没有则动态计算
        BigDecimal totalRefundAmount = BigDecimal.ZERO;
        List<StudentFeeDetail.RefundSegment> refundSegments = new ArrayList<>();
        int totalLeaveDays = 0;

        try {
            // 先尝试获取已保存的退费记录
            RefundCalculateResult refundResult = refundService.getSavedRefund(student.getId(), semesterId);
            
            // 如果没有保存的记录，则动态计算
            if (refundResult == null || refundResult.getTotalRefundAmount() == null 
                    || refundResult.getTotalRefundAmount().compareTo(BigDecimal.ZERO) == 0) {
                refundResult = refundService.calculateRefund(student.getId(), semesterId, null, null);
            }
            
            totalRefundAmount = refundResult.getTotalRefundAmount();
            totalLeaveDays = refundResult.getTotalLeaveDays() != null ? refundResult.getTotalLeaveDays() : 0;

            // 转换退费区间明细
            if (refundResult.getLeaveSegments() != null) {
                for (LeaveSegment segment : refundResult.getLeaveSegments()) {
                    StudentFeeDetail.RefundSegment rs = new StudentFeeDetail.RefundSegment();
                    rs.setStartDate(segment.getStartDate());
                    rs.setEndDate(segment.getEndDate());
                    rs.setDays(segment.getDays());
                    rs.setMatchedRuleType(segment.getMatchedRuleType());
                    rs.setSegmentAmount(segment.getSegmentAmount());

                    // 转换退费明细项
                    if (segment.getRefundItems() != null) {
                        List<StudentFeeDetail.RefundItem> items = new ArrayList<>();
                        for (RefundItem item : segment.getRefundItems()) {
                            StudentFeeDetail.RefundItem ri = new StudentFeeDetail.RefundItem();
                            ri.setFeeTypeCode(item.getFeeTypeCode());
                            ri.setFeeTypeName(item.getFeeTypeName());
                            ri.setDailyRate(item.getDailyRate());
                            ri.setAmount(item.getAmount());
                            items.add(ri);
                        }
                        rs.setRefundItems(items);
                    }
                    refundSegments.add(rs);
                }
            }
        } catch (Exception e) {
            // 退费计算失败时忽略，按0处理
        }

        // 构建费用类型明细
        List<StudentFeeDetail.FeeTypeDetail> feeTypeDetails = new ArrayList<>();
        BigDecimal totalPaid = BigDecimal.ZERO;

        for (FeeType feeType : feeTypes) {
            StudentFeeDetail.FeeTypeDetail ftDetail = new StudentFeeDetail.FeeTypeDetail();
            ftDetail.setFeeTypeId(feeType.getId());
            ftDetail.setTypeCode(feeType.getTypeCode());
            ftDetail.setFeeTypeName(feeType.getTypeName());

            BigDecimal paid = paidByType.getOrDefault(feeType.getId(), BigDecimal.ZERO);

            ftDetail.setPaidAmount(paid);
            ftDetail.setRefundAmount(BigDecimal.ZERO); // 退费不按费用类型分组
            ftDetail.setActualAmount(paid);

            feeTypeDetails.add(ftDetail);

            totalPaid = totalPaid.add(paid);
        }

        detail.setFeeTypeDetails(feeTypeDetails);
        detail.setTotalPaid(totalPaid);
        detail.setTotalRefund(totalRefundAmount);
        detail.setActualAmount(totalPaid.subtract(totalRefundAmount));
        detail.setRefundSegments(refundSegments);

        // 获取考勤统计（请假天数从退费计算结果获取）
        detail.setAttendanceSummary(buildAttendanceSummary(student.getId(), semester, totalLeaveDays));

        return detail;
    }

    /**
     * 构建考勤统计
     *
     * @param studentId 学生ID
     * @param semester 学期
     * @param leaveDaysFromRefund 从退费计算获取的请假天数（用于显示，实际统计从考勤表获取）
     */
    private StudentFeeDetail.AttendanceSummary buildAttendanceSummary(Long studentId, Semester semester, int leaveDaysFromRefund) {
        StudentFeeDetail.AttendanceSummary summary = new StudentFeeDetail.AttendanceSummary();

        if (semester == null) {
            summary.setPresentDays(0);
            summary.setLeaveDays(0);
            summary.setAbsentDays(0);
            summary.setTotalDays(0);
            summary.setAttendanceRate("0%");
            return summary;
        }

        // 查询学期内的考勤记录
        List<Attendance> attendances = attendanceMapper.selectList(
                new LambdaQueryWrapper<Attendance>()
                        .eq(Attendance::getStudentId, studentId)
                        .ge(Attendance::getAttendDate, semester.getStartDate())
                        .le(Attendance::getAttendDate, semester.getEndDate()));

        int presentDays = 0;
        int leaveDays = 0;
        int absentDays = 0;

        for (Attendance att : attendances) {
            switch (att.getStatus()) {
                case "present" -> presentDays++;
                case "leave" -> leaveDays++;
                case "absent" -> absentDays++;
            }
        }

        int totalDays = presentDays + leaveDays + absentDays;
        String rate = totalDays > 0
                ? String.format("%.1f%%", (double) presentDays / totalDays * 100)
                : "0%";

        summary.setPresentDays(presentDays);
        summary.setLeaveDays(leaveDays);
        summary.setAbsentDays(absentDays);
        summary.setTotalDays(totalDays);
        summary.setAttendanceRate(rate);

        return summary;
    }

}
