package com.kindergarten.system.dto;

import lombok.Data;

@Data
public class StudentExportQuery {

    private String name;

    private Long classId;

    private String status;
}