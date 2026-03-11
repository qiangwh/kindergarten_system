package com.kindergarten.system.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SemesterFeeSummary {

    private Long semesterId;
    private BigDecimal totalAmount;
    private List<FeeTypeSummaryItem> byFeeType;
}