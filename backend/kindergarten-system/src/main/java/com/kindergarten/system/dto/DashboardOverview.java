/**
 * 首页统计概览DTO
 * <p>
 * 封装首页展示的各项统计数据，包括：
 * - 学生统计（总数、各班级人数）
 * - 当前学期信息
 * - 收费统计（总额、按费用类型分组）
 * - 本月考勤概览
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
package com.kindergarten.system.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardOverview implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 在读学生总数 */
    private Long activeStudentCount;

    /** 各班级学生人数 */
    private List<ClassStudentCount> classStudentCounts;

    /** 当前学期 */
    private CurrentSemester currentSemester;

    /** 本学期收费总额 */
    private BigDecimal currentSemesterFeeTotal;

    /** 本学期各费用类型收费金额 */
    private List<FeeTypeAmount> currentSemesterFeeByType;

    /** 本月考勤概览 */
    private AttendanceOverview attendanceThisMonth;

    /**
     * 班级学生人数
     */
    @Data
    public static class ClassStudentCount implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 班级ID */
        private Long classId;

        /** 班级名称 */
        private String className;

        /** 学生人数 */
        private Long count;
    }

    /**
     * 当前学期信息
     */
    @Data
    public static class CurrentSemester implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 学期ID */
        private Long id;

        /** 学期名称 */
        private String semesterName;
    }

    /**
     * 费用类型金额
     */
    @Data
    public static class FeeTypeAmount implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 费用类型编码 */
        private String typeCode;

        /** 费用类型名称 */
        private String feeTypeName;

        /** 收费金额 */
        private BigDecimal amount;
    }

    /**
     * 考勤概览
     */
    @Data
    public static class AttendanceOverview implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 出勤天数 */
        private Long presentDays;

        /** 请假天数 */
        private Long leaveDays;

        /** 缺勤天数 */
        private Long absentDays;
    }

}
