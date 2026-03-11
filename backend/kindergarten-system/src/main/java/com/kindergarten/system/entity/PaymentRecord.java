/**
 * 缴费记录实体类
 * <p>
 * 对应数据库表：payment_record
 * 存储学生的缴费信息，包括缴费金额、缴费日期、收据信息等
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
package com.kindergarten.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 缴费记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("payment_record")
public class PaymentRecord extends BaseEntity {

    /** 学生ID */
    private Long studentId;

    /** 学期ID */
    private Long semesterId;

    /** 费用类型ID */
    private Long feeTypeId;

    /** 缴费金额 */
    private BigDecimal amount;

    /** 缴费日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate payDate;

    /** 收据编号 */
    private String receiptNo;

    /** 收据图片URL */
    private String receiptImageUrl;

    /** 备注 */
    private String remark;

    /** 学生姓名（非数据库字段，用于展示） */
    @TableField(exist = false)
    private String studentName;

    /** 班级ID（非数据库字段，用于查询） */
    @TableField(exist = false)
    private Long classId;

    /** 班级名称（非数据库字段，用于展示） */
    @TableField(exist = false)
    private String className;

    /** 学期名称（非数据库字段，用于展示） */
    @TableField(exist = false)
    private String semesterName;

    /** 费用类型名称（非数据库字段，用于展示） */
    @TableField(exist = false)
    private String feeTypeName;

    /** 费用类型编码（非数据库字段，用于展示） */
    @TableField(exist = false)
    private String typeCode;
}