package com.kindergarten.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AttendanceListQuery {

    private Long classId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
}