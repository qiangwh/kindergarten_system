package com.kindergarten.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kindergarten.system.dto.DashboardOverview;
import com.kindergarten.system.dto.DashboardOverview.AttendanceOverview;
import com.kindergarten.system.dto.DashboardOverview.ClassStudentCount;
import com.kindergarten.system.dto.DashboardOverview.CurrentSemester;
import com.kindergarten.system.dto.DashboardOverview.FeeTypeAmount;
import com.kindergarten.system.dto.AttendanceMonthStatItem;
import com.kindergarten.system.dto.FeeTypeSummaryItem;
import com.kindergarten.system.entity.*;
import com.kindergarten.system.mapper.*;
import com.kindergarten.system.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
@CacheConfig(cacheNames = "dashboard")
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final StudentMapper studentMapper;
    private final ClassInfoMapper classInfoMapper;
    private final SemesterMapper semesterMapper;
    private final FeeSummaryMapper feeSummaryMapper;
    private final AttendanceMapper attendanceMapper;

    /**
     * 首页概览数据属于典型的“读多写少”场景，
     * 同一个月内频繁打开首页时，直接复用缓存结果可以减少多次聚合查询。
     * 写操作会在相关服务中统一触发失效，所以这里缓存的是整份首页快照。
     */
    @Override
    @Cacheable(key = "T(com.kindergarten.system.common.cache.CacheKeyUtil).key('overview', T(java.time.LocalDate).now().getYear(), T(java.time.LocalDate).now().getMonthValue())", sync = true)
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
     * 加载学生统计。
     * <p>
     * 这一块同时用于首页人数展示和班级分布展示，数据变化通常来自学生新增、编辑、导入或状态变更。
     * </p>
     */
    private void loadStudentStats(DashboardOverview overview) {
        // 在读学生总数
        Long activeCount = studentMapper.selectCount(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getStatus, "active")
        );
        overview.setActiveStudentCount(activeCount);

        // 各班级人数（一次聚合查询，避免 N+1）
        List<ClassStudentCount> classCounts = studentMapper.selectActiveStudentCountsByClass();
        if (classCounts == null) {
            classCounts = new ArrayList<>();
        }
        overview.setClassStudentCounts(classCounts);
    }

    /**
     * 加载当前学期。
     * <p>
     * 当前学期切换后会由学期服务统一清空 dashboard / feeSummary 缓存。
     * </p>
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
     * 加载收费统计。
     * <p>
     * 这里依赖当前学期，缓存命中后可以避免重复执行收费汇总查询。
     * </p>
     */
    private void loadFeeStats(DashboardOverview overview) {
        if (overview.getCurrentSemester() == null) {
            overview.setCurrentSemesterFeeTotal(BigDecimal.ZERO);
            overview.setCurrentSemesterFeeByType(new ArrayList<>());
            return;
        }

        Long semesterId = overview.getCurrentSemester().getId();

        overview.setCurrentSemesterFeeTotal(
                java.util.Optional.ofNullable(feeSummaryMapper.selectSemesterTotal(semesterId))
                        .orElse(BigDecimal.ZERO)
        );

        List<FeeTypeSummaryItem> byFeeType = feeSummaryMapper.selectSemesterByFeeType(semesterId);
        List<FeeTypeAmount> feeTypeAmounts = new ArrayList<>();
        if (byFeeType != null) {
            for (FeeTypeSummaryItem feeTypeSummaryItem : byFeeType) {
                FeeTypeAmount item = new FeeTypeAmount();
                item.setTypeCode(feeTypeSummaryItem.getTypeCode());
                item.setFeeTypeName(feeTypeSummaryItem.getFeeTypeName());
                item.setAmount(feeTypeSummaryItem.getAmount());
                feeTypeAmounts.add(item);
            }
        }
        overview.setCurrentSemesterFeeByType(feeTypeAmounts);
    }

    /**
     * 加载考勤统计（本月）。
     * <p>
     * 由于首页默认展示当月数据，所以缓存 key 额外按“年-月”区分，跨月自动生成新缓存。
     * </p>
     */
    private void loadAttendanceStats(DashboardOverview overview) {
        LocalDate now = LocalDate.now();
        String startDate = now.withDayOfMonth(1).toString();
        String endDate = now.withDayOfMonth(now.lengthOfMonth()).toString();

        // 本月考勤概览（直接用数据库聚合）
        AttendanceMonthStatItem monthSummary = attendanceMapper.selectMonthSummary(null, startDate, endDate);

        AttendanceOverview attendanceOverview = new AttendanceOverview();
        attendanceOverview.setPresentDays(monthSummary == null || monthSummary.getPresentDays() == null ? 0L : monthSummary.getPresentDays().longValue());
        attendanceOverview.setLeaveDays(monthSummary == null || monthSummary.getLeaveDays() == null ? 0L : monthSummary.getLeaveDays().longValue());
        attendanceOverview.setAbsentDays(monthSummary == null || monthSummary.getAbsentDays() == null ? 0L : monthSummary.getAbsentDays().longValue());
        overview.setAttendanceThisMonth(attendanceOverview);
    }

}
