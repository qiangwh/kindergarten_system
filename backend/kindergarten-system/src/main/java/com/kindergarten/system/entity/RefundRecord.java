package com.kindergarten.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 退费记录实体类
 * <p>
 * 对应数据库表：refund_record
 * 存储退费结果快照，支持两种退费类型：
 * - LEAVE：请假退费，基于连续请假天数计算
 * - DROPOUT：离园退费，基于学期中途离园计算
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("refund_record")
public class RefundRecord extends BaseEntity {

    /** 学生ID */
    private Long studentId;

    /** 学期ID */
    private Long semesterId;

    /**
     * 退费类型
     * <ul>
     *   <li>LEAVE - 请假退费：基于连续请假天数计算</li>
     *   <li>DROPOUT - 离园退费：基于学期中途离园计算</li>
     * </ul>
     */
    private String refundType;

    /** 请假总天数（请假退费时使用） */
    private Integer leaveDaysTotal;

    /** 实际在园天数（离园退费时使用） */
    private Integer actualAttendDays;

    /** 应退天数（离园退费时使用） */
    private Integer refundDays;

    /** 离园日期（离园退费时使用） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate leaveDate;

    /** 退费金额 */
    private BigDecimal refundAmount;

    /** 分段明细快照（JSON） */
    @TableField("detail_json")
    private String detailJson;

    /** 计算时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime calculatedAt;

    /** 计算人ID */
    private Long calculatedBy;

    // ==================== 非数据库字段 ====================

    /** 学生姓名 */
    @TableField(exist = false)
    private String studentName;

    /** 班级名称 */
    @TableField(exist = false)
    private String className;

    /** 学期名称 */
    @TableField(exist = false)
    private String semesterName;
}