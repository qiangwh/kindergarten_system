package com.kindergarten.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kindergarten.system.dto.DashboardOverview;
import com.kindergarten.system.dto.DashboardOverview.AttendanceOverview;
import com.kindergarten.system.dto.DashboardOverview.ClassStudentCount;
import com.kindergarten.system.dto.DashboardOverview.CurrentSemester;
import com.kindergarten.system.dto.DashboardOverview.FeeTypeAmount;
import com.kindergarten.system.entity.*;
import com.kindergarten.system.mapper.*;
import com.kindergarten.system.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final StudentMapper studentMapper;
    private final ClassInfoMapper classInfoMapper;
    private final SemesterMapper semesterMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final FeeTypeMapper feeTypeMapper;
    private final AttendanceMapper attendanceMapper;

    @Override
    public DashboardOverview getOverview() {
        DashboardOverview overview = new DashboardOverview();

        // 1. 学生统计
        loadStudentStats(overview);

        // 2. 当前学期
        loadCurrentSemester(overview);

        // 3. 收费统计
        loadFeeStats(overview);

        // 4. 本月考勤概览
        loadAttendanceStats(overview);

        return overview;
    }

    /**
     * 加载学生统计
     */
    private void loadStudentStats(DashboardOverview overview) {
        // 在读学生总数
        Long activeCount = studentMapper.selectCount(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getStatus, "active")
        );
        overview.setActiveStudentCount(activeCount);

        // 各班级人数
        List<ClassInfo> classes = classInfoMapper.selectList(
                new LambdaQueryWrapper<ClassInfo>()
                        .eq(ClassInfo::getStatus, 1)
                        .orderByDesc(ClassInfo::getGradeYear)
                        .orderByAsc(ClassInfo::getId)
        );

        List<ClassStudentCount> classCounts = new ArrayList<>();
        for (ClassInfo classInfo : classes) {
            Long count = studentMapper.selectCount(
                    new LambdaQueryWrapper<Student>()
                            .eq(Student::getClassId, classInfo.getId())
                            .eq(Student::getStatus, "active")
            );

            ClassStudentCount item = new ClassStudentCount();
            item.setClassId(classInfo.getId());
            item.setClassName(classInfo.getClassName());
            item.setCount(count);
            classCounts.add(item);
        }
        overview.setClassStudentCounts(classCounts);
    }

    /**
     * 加载当前学期
     */
    private void loadCurrentSemester(DashboardOverview overview) {
        Semester current = semesterMapper.selectOne(
                new LambdaQueryWrapper<Semester>()
                        .eq(Semester::getIsCurrent, 1)
                        .eq(Semester::getStatus, 1)
                        .last("LIMIT 1")
        );

        if (current != null) {
            CurrentSemester cs = new CurrentSemester();
            cs.setId(current.getId());
            cs.setSemesterName(current.getSemesterName());
            overview.setCurrentSemester(cs);
        }
    }

    /**
     * 加载收费统计
     */
    private void loadFeeStats(DashboardOverview overview) {
        if (overview.getCurrentSemester() == null) {
            overview.setCurrentSemesterFeeTotal(BigDecimal.ZERO);
            overview.setCurrentSemesterFeeByType(new ArrayList<>());
            return;
        }

        Long semesterId = overview.getCurrentSemester().getId();

        // 本学期总收费
        List<PaymentRecord> records = paymentRecordMapper.selectList(
                new LambdaQueryWrapper<PaymentRecord>()
                        .eq(PaymentRecord::getSemesterId, semesterId)
        );

        BigDecimal total = records.stream()
                .map(PaymentRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        overview.setCurrentSemesterFeeTotal(total);

        // 按费用类型分组
        Map<Long, BigDecimal> amountByFeeType = records.stream()
                .collect(Collectors.groupingBy(
                        PaymentRecord::getFeeTypeId,
                        Collectors.reducing(BigDecimal.ZERO, PaymentRecord::getAmount, BigDecimal::add)
                ));

        // 获取费用类型信息
        List<FeeType> feeTypes = feeTypeMapper.selectList(
                new LambdaQueryWrapper<FeeType>()
                        .eq(FeeType::getStatus, 1)
        );

        List<FeeTypeAmount> feeTypeAmounts = new ArrayList<>();
        for (FeeType feeType : feeTypes) {
            FeeTypeAmount item = new FeeTypeAmount();
            item.setTypeCode(feeType.getTypeCode());
            item.setFeeTypeName(feeType.getTypeName());
            item.setAmount(amountByFeeType.getOrDefault(feeType.getId(), BigDecimal.ZERO));
            feeTypeAmounts.add(item);
        }
        overview.setCurrentSemesterFeeByType(feeTypeAmounts);
    }

    /**
     * 加载考勤统计（本月）
     */
    private void loadAttendanceStats(DashboardOverview overview) {
        LocalDate now = LocalDate.now();
        String startDate = now.withDayOfMonth(1).toString();
        String endDate = now.withDayOfMonth(now.lengthOfMonth()).toString();

        // 查询本月所有考勤记录
        List<Attendance> attendances = attendanceMapper.selectList(
                new LambdaQueryWrapper<Attendance>()
                        .between(Attendance::getAttendDate, startDate, endDate)
        );

        long presentCount = attendances.stream()
                .filter(a -> "present".equals(a.getStatus()))
                .count();
        long leaveCount = attendances.stream()
                .filter(a -> "leave".equals(a.getStatus()))
                .count();
        long absentCount = attendances.stream()
                .filter(a -> "absent".equals(a.getStatus()))
                .count();

        AttendanceOverview attendanceOverview = new AttendanceOverview();
        attendanceOverview.setPresentDays(presentCount);
        attendanceOverview.setLeaveDays(leaveCount);
        attendanceOverview.setAbsentDays(absentCount);
        overview.setAttendanceThisMonth(attendanceOverview);
    }

}
