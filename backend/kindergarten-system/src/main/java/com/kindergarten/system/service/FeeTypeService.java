package com.kindergarten.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kindergarten.system.entity.FeeType;

import java.util.List;

public interface FeeTypeService extends IService<FeeType> {

    List<FeeType> listEnabled();

    /**
     * 分页查询收费类型列表
     *
     * @param typeName 费用类型名称（模糊查询）
     * @param page     页码
     * @param pageSize 每页条数
     * @return 分页数据
     */
    IPage<FeeType> pageList(String typeName, Long page, Long pageSize);
}