package com.kindergarten.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kindergarten.system.entity.ClassInfo;
import com.kindergarten.system.mapper.ClassInfoMapper;
import com.kindergarten.system.service.ClassInfoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassInfoServiceImpl extends ServiceImpl<ClassInfoMapper, ClassInfo> implements ClassInfoService {

    @Override
    public List<ClassInfo> listEnabled() {
        return list(new LambdaQueryWrapper<ClassInfo>()
                .eq(ClassInfo::getStatus, 1)
                .orderByAsc(ClassInfo::getSortOrder)
                .orderByAsc(ClassInfo::getId));
    }
}