package com.kindergarten.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fee_type")
public class FeeType extends BaseEntity {

    private String typeCode;
    private String typeName;
    private String description;
    private Integer sortOrder;
    private Integer status;
}