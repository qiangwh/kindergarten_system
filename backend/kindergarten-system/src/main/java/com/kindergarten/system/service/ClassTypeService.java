package com.kindergarten.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kindergarten.system.entity.ClassType;

import java.util.List;

/**
 * 班级类型服务接口
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
public interface ClassTypeService extends IService<ClassType> {

    /**
     * 获取启用的班级类型列表
     *
     * @return 班级类型列表
     */
    List<ClassType> listEnabled();

}
