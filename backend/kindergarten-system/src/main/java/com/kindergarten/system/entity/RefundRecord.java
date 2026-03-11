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
@TableName("refund_record")
public class RefundRecord extends BaseEntity {

    private Long studentId;
    private Long semesterId;
    private Long feeTypeId;
    private Long paymentRecordId;
    private Integer absentDays;
    private BigDecimal refundAmount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate refundDate;

    private String remark;

    @TableField(exist = false)
    private String studentName;

    @TableField(exist = false)
    private String className;

    @TableField(exist = false)
    private String semesterName;

    @TableField(exist = false)
    private String feeTypeName;
}