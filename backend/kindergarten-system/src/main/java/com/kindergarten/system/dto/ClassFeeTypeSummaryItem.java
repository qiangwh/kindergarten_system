package com.kindergarten.system.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ClassFeeTypeSummaryItem {

    private Long classId;
    private String className;
    private Long feeTypeId;
    private String feeTypeName;
    private String typeCode;
    private BigDecimal amount;
}