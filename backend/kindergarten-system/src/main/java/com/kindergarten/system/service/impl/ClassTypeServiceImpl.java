package com.kindergarten.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kindergarten.system.entity.ClassType;
import com.kindergarten.system.mapper.ClassTypeMapper;
import com.kindergarten.system.service.ClassTypeService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 班级类型服务实现类
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
@Service
public class ClassTypeServiceImpl extends ServiceImpl<ClassTypeMapper, ClassType> implements ClassTypeService {

    @Override
    public List<ClassType> listEnabled() {
        return baseMapper.selectList(
                new LambdaQueryWrapper<ClassType>()
                        .eq(ClassType::getStatus, 1)
                        .orderByAsc(ClassType::getSortOrder)
        );
    }

}
