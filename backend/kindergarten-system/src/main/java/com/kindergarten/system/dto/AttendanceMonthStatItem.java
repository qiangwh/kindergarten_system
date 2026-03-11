package com.kindergarten.system.dto;

import lombok.Data;

@Data
public class AttendanceMonthStatItem {

    private Long classId;
    private String month;
    private Integer presentDays;
    private Integer leaveDays;
    private Integer absentDays;
    private Integer totalDays;
}