/**
 * 退费计算结果DTO
 * <p>
 * 封装退费计算的完整结果，包括：
 * - 学生基本信息
 * - 学期信息
 * - 连续请假区间明细
 * - 各区间退费金额
 * - 总退费金额
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
public class RefundCalculateResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 学生ID */
    private Long studentId;

    /** 学生姓名 */
    private String studentName;

    /** 学期ID */
    private Long semesterId;

    /** 学期名称 */
    private String semesterName;

    /** 连续请假区间列表 */
    private List<LeaveSegment> leaveSegments;

    /** 总退费金额 */
    private BigDecimal totalRefundAmount;

    /**
     * 请假区间
     */
    @Data
    public static class LeaveSegment implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 开始日期 */
        private String startDate;

        /** 结束日期 */
        private String endDate;

        /** 请假天数 */
        private Integer days;

        /** 匹配的规则类型：LEAVE_5_9 / LEAVE_10_PLUS / NONE */
        private String matchedRuleType;

        /** 退费明细列表 */
        private List<RefundItem> refundItems;

        /** 该区间的退费金额 */
        private BigDecimal segmentAmount;
    }

    /**
     * 退费明细项
     */
    @Data
    public static class RefundItem implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 费用类型编码：meal / education */
        private String feeTypeCode;

        /** 费用类型名称 */
        private String feeTypeName;

        /** 日均退费金额 */
        private BigDecimal dailyRate;

        /** 该项退费金额 */
        private BigDecimal amount;
    }

}
