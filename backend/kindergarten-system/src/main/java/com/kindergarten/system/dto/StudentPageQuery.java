package com.kindergarten.system.dto;

import lombok.Data;

@Data
public class StudentPageQuery {

    private Long page = 1L;
    private Long pageSize = 10L;
    private String name;
    private Long classId;
    private String status;
}