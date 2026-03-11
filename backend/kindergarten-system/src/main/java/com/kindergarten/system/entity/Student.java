package com.kindergarten.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("student")
public class Student extends BaseEntity {

    private String name;
    private String gender;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    private Long classId;
    private String parentName;
    private String parentPhone;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate enrollDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate leaveDate;

    private String status;
    private String remark;

    @TableField(exist = false)
    private String className;
}