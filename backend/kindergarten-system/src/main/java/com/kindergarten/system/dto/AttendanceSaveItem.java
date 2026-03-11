package com.kindergarten.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AttendanceSaveItem {

    @NotNull
    private Long studentId;

    @NotBlank
    private String status;

    private String remark;
}