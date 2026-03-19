package com.kindergarten.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kindergarten.system.entity.ClassType;
import com.kindergarten.system.mapper.ClassTypeMapper;
import com.kindergarten.system.service.ClassTypeService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

/**
 * 班级类型服务实现类
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
@Service
@CacheConfig(cacheNames = "classType")
public class ClassTypeServiceImpl extends ServiceImpl<ClassTypeMapper, ClassType> implements ClassTypeService {

    @Override
    @Cacheable(key = "'all'")
    public List<ClassType> list() {
        return baseMapper.selectList(
                new LambdaQueryWrapper<ClassType>()
                        .orderByAsc(ClassType::getSortOrder)
                        .orderByAsc(ClassType::getId)
        );
    }

    @Override
    @Cacheable(key = "'enabled'")
    public List<ClassType> listEnabled() {
        return baseMapper.selectList(
                new LambdaQueryWrapper<ClassType>()
                        .eq(ClassType::getStatus, 1)
                        .orderByAsc(ClassType::getSortOrder)
                        .orderByAsc(ClassType::getId)
        );
    }

    @Override
    @CacheEvict(cacheNames = {"classType", "classInfo"}, allEntries = true)
    public boolean save(ClassType entity) {
        return super.save(entity);
    }

    @Override
    @CacheEvict(cacheNames = {"classType", "classInfo"}, allEntries = true)
    public boolean updateById(ClassType entity) {
        return super.updateById(entity);
    }

    @Override
    @CacheEvict(cacheNames = {"classType", "classInfo"}, allEntries = true)
    public boolean removeById(Serializable id) {
        return super.removeById(id);
    }

}
