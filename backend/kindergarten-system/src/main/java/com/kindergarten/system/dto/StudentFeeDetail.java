package com.kindergarten.system.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 学生收费明细 DTO
 * <p>
 * 展示每个学生在当前学期的详细费用信息，包括：
 * - 学生基本信息
 * - 各项费用明细（学费、餐费等）
 * - 退费金额及明细（连续请假区间）
 * - 考勤统计
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
@Data
public class StudentFeeDetail {

    /** 学生ID */
    private Long studentId;

    /** 学生姓名 */
    private String studentName;

    /** 班级ID */
    private Long classId;

    /** 班级名称 */
    private String className;

    /** 学期ID */
    private Long semesterId;

    /** 学期名称 */
    private String semesterName;

    /** 总缴费金额 */
    private BigDecimal totalPaid;

    /** 总退费金额（按退费规则计算） */
    private BigDecimal totalRefund;

    /** 实际应收金额（总缴费 - 总退费） */
    private BigDecimal actualAmount;

    /** 各费用类型明细 */
    private List<FeeTypeDetail> feeTypeDetails;

    /** 退费明细（连续请假区间） */
    private List<RefundSegment> refundSegments;

    /** 考勤统计 */
    private AttendanceSummary attendanceSummary;

    // ==================== 内部类 ====================

    /**
     * 费用类型明细
     */
    @Data
    public static class FeeTypeDetail {

        /** 费用类型ID */
        private Long feeTypeId;

        /** 费用类型编码 */
        private String typeCode;

        /** 费用类型名称 */
        private String feeTypeName;

        /** 缴费金额 */
        private BigDecimal paidAmount;

        /** 退费金额 */
        private BigDecimal refundAmount;

        /** 实际金额 */
        private BigDecimal actualAmount;
    }

    /**
     * 退费区间明细
     */
    @Data
    public static class RefundSegment {

        /** 开始日期 */
        private String startDate;

        /** 结束日期 */
        private String endDate;

        /** 请假天数 */
        private Integer days;

        /** 匹配规则：LEAVE_5_9 / LEAVE_10_PLUS / NONE */
        private String matchedRuleType;

        /** 退费明细项 */
        private List<RefundItem> refundItems;

        /** 该区间退费金额 */
        private BigDecimal segmentAmount;
    }

    /**
     * 退费明细项
     */
    @Data
    public static class RefundItem {

        /** 费用类型编码 */
        private String feeTypeCode;

        /** 费用类型名称 */
        private String feeTypeName;

        /** 日均退费金额 */
        private BigDecimal dailyRate;

        /** 该项退费金额 */
        private BigDecimal amount;
    }

    /**
     * 考勤统计
     */
    @Data
    public static class AttendanceSummary {

        /** 出勤天数 */
        private Integer presentDays;

        /** 请假天数（连续请假可退费的天数） */
        private Integer leaveDays;

        /** 缺勤天数 */
        private Integer absentDays;

        /** 总天数 */
        private Integer totalDays;

        /** 出勤率 */
        private String attendanceRate;
    }

}
