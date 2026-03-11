package com.kindergarten.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AttendanceMonthStatQuery {

    @NotNull
    private Long classId;
}