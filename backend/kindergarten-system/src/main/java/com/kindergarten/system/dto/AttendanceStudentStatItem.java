package com.kindergarten.system.dto;

import lombok.Data;

@Data
public class AttendanceStudentStatItem {

    private Long studentId;
    private String studentName;
    private Long classId;
    private String className;
    private Integer presentDays;
    private Integer leaveDays;
    private Integer absentDays;
    private Integer totalDays;
}