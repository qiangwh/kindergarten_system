/**
 * 学生实体类
 * <p>
 * 对应数据库表：student
 * 存储幼儿园学生的基本信息，包括个人资料、家长信息、入园离园日期等
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
package com.kindergarten.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 学生信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("student")
public class Student extends BaseEntity {

    /** 学生姓名 */
    private String name;

    /** 性别：M-男，F-女 */
    private String gender;

    /** 出生日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    /** 班级ID */
    private Long classId;

    /** 学号 */
    private String studentNo;

    /** 家长姓名 */
    private String parentName;

    /** 家长电话 */
    private String parentPhone;

    /** 家长微信 */
    private String parentWechat;

    /** 入园日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate enrollDate;

    /** 离园日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @TableField(updateStrategy = com.baomidou.mybatisplus.annotation.FieldStrategy.ALWAYS)
    private LocalDate leaveDate;

    /** 状态：active-在读，inactive-离园 */
    private String status;

    /** 备注 */
    private String remark;

    /** 班级名称（非数据库字段，用于展示） */
    @TableField(exist = false)
    private String className;
}