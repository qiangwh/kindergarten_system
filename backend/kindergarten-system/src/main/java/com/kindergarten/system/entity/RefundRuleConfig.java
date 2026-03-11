package com.kindergarten.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("refund_rule_config")
public class RefundRuleConfig extends BaseEntity {

    private String ruleType;
    private String feeTypeCode;
    private BigDecimal dailyRate;
    private Integer minDays;
    private Integer maxDays;
    private Long semesterId;
    private Integer status;
}