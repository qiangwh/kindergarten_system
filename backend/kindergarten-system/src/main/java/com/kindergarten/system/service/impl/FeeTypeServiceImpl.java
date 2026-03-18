package com.kindergarten.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kindergarten.system.entity.FeeType;
import com.kindergarten.system.mapper.FeeTypeMapper;
import com.kindergarten.system.service.FeeTypeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeeTypeServiceImpl extends ServiceImpl<FeeTypeMapper, FeeType> implements FeeTypeService {

    @Override
    public List<FeeType> listEnabled() {
        return list(new LambdaQueryWrapper<FeeType>()
                .eq(FeeType::getStatus, 1)
                .orderByAsc(FeeType::getSortOrder)
                .orderByAsc(FeeType::getId));
    }

    @Override
    public IPage<FeeType> pageList(String typeName, Long page, Long pageSize) {
        long current = page == null || page < 1 ? 1 : page;
        long size = pageSize == null || pageSize < 1 ? 10 : pageSize;

        Page<FeeType> feeTypePage = new Page<>(current, size);
        return page(feeTypePage, new LambdaQueryWrapper<FeeType>()
                .eq(FeeType::getStatus, 1)
                .like(typeName != null && !typeName.isBlank(), FeeType::getTypeName, typeName)
                .orderByAsc(FeeType::getSortOrder)
                .orderByAsc(FeeType::getId));
    }
}