package com.kindergarten.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("payment_record")
public class PaymentRecord extends BaseEntity {

    private Long studentId;
    private Long semesterId;
    private Long feeTypeId;
    private BigDecimal amount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate payDate;

    private String receiptNo;
    private String receiptImageUrl;
    private String remark;

    @TableField(exist = false)
    private String studentName;

    @TableField(exist = false)
    private Long classId;

    @TableField(exist = false)
    private String className;

    @TableField(exist = false)
    private String semesterName;

    @TableField(exist = false)
    private String feeTypeName;

    @TableField(exist = false)
    private String typeCode;
}