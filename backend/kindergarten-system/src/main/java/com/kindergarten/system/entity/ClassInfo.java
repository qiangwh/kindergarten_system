/**
 * 班级实体类
 * <p>
 * 对应数据库表：class_info
 * 存储具体班级信息，如：小班1班、中班2班等
 * 关联班级类型（小班、中班、大班）
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
package com.kindergarten.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 班级信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("class_info")
public class ClassInfo extends BaseEntity {

    /** 班级类型ID */
    private Long classTypeId;

    /** 班级名称：小班1班、中班2班等 */
    private String className;

    /** 年级年份：如2026 */
    private String gradeYear;

    /** 最大学生数 */
    private Integer maxStudents;

    /** 当前学生数 */
    private Integer currentCount;

    /** 状态：1-启用，0-禁用 */
    private Integer status;

    /** 班级类型名称（非数据库字段，用于展示） */
    @TableField(exist = false)
    private String classTypeName;

}