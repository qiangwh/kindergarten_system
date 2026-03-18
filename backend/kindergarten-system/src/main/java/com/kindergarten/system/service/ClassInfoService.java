/**
 * 班级服务接口
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
package com.kindergarten.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kindergarten.system.entity.ClassInfo;

import java.util.List;

public interface ClassInfoService extends IService<ClassInfo> {

    /**
     * 获取启用的班级列表（含班级类型名称）
     *
     * @return 班级列表
     */
    List<ClassInfo> listEnabled();

    /**
     * 根据班级类型获取班级列表
     *
     * @param classTypeId 班级类型ID
     * @return 班级列表
     */
    List<ClassInfo> listByClassType(Long classTypeId);

    /**
     * 分页查询班级列表
     *
     * @param className   班级名称（模糊查询）
     * @param classTypeId 班级类型ID
     * @param page        页码
     * @param pageSize    每页条数
     * @return 班级分页数据
     */
    IPage<ClassInfo> pageClasses(String className, Long classTypeId, Long page, Long pageSize);

}