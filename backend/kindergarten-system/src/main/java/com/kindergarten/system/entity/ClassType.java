package com.kindergarten.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 班级类型实体类
 * <p>
 * 对应数据库表：class_type
 * 存储班级类型信息，如：小班、中班、大班
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("class_type")
public class ClassType extends BaseEntity {

    /** 类型名称：小班、中班、大班 */
    private String typeName;

    /** 排序 */
    private Integer sortOrder;

    /** 状态：1-启用，0-禁用 */
    private Integer status;

}
