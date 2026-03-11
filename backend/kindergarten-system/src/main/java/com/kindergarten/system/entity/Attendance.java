package com.kindergarten.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("attendance")
public class Attendance extends BaseEntity {

    private Long studentId;
    private Long classId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate attendDate;

    private String status;
    private String remark;

    @TableField(exist = false)
    private String studentName;

    @TableField(exist = false)
    private String className;
}