package com.kindergarten.system.dto;

import lombok.Data;

/**
 * 退费规则配置查询参数
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
@Data
public class RefundRuleConfigQuery {

    /** 规则类型（LEAVE_5_9 / LEAVE_10_PLUS） */
    private String ruleType;

    /** 费用类型编码 */
    private String feeTypeCode;

    /** 学期ID */
    private Long semesterId;

    /** 页码 */
    private Long page = 1L;

    /** 每页条数 */
    private Long pageSize = 20L;
}
