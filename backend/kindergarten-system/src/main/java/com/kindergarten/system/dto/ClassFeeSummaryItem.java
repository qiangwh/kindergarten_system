package com.kindergarten.system.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ClassFeeSummaryItem {

    private Long classId;
    private String className;
    private BigDecimal amount;
}