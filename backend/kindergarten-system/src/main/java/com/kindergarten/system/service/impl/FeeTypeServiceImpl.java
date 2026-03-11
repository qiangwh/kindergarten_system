package com.kindergarten.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
}