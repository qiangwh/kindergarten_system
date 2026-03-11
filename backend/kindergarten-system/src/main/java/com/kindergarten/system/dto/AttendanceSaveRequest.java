package com.kindergarten.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AttendanceSaveRequest {

    @NotNull
    private Long classId;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate attendDate;

    @Valid
    @NotEmpty
    private List<AttendanceSaveItem> items;
}