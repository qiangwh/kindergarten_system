package com.kindergarten.system.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SemesterFeeCompareItem {

    private Long semesterId;
    private String semesterName;
    private BigDecimal totalAmount;
}