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
 * 存储退费结果快照
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

    /** 请假总天数 */
    private Integer leaveDaysTotal;

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