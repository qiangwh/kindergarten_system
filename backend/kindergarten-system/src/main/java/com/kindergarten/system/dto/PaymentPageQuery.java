package com.kindergarten.system.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PaymentPageQuery {

    private Long page = 1L;
    private Long pageSize = 10L;
    private String studentName;
    private Long studentId;
    private Long semesterId;
    private Long feeTypeId;
    private Long classId;
    private LocalDate payDateFrom;
    private LocalDate payDateTo;
}