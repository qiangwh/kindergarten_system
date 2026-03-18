/**
 * 班级服务实现类
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
package com.kindergarten.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kindergarten.system.entity.ClassInfo;
import com.kindergarten.system.entity.ClassType;
import com.kindergarten.system.mapper.ClassInfoMapper;
import com.kindergarten.system.mapper.ClassTypeMapper;
import com.kindergarten.system.service.ClassInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassInfoServiceImpl extends ServiceImpl<ClassInfoMapper, ClassInfo> implements ClassInfoService {

    /** 班级类型 Mapper */
    private final ClassTypeMapper classTypeMapper;

    @Override
    public List<ClassInfo> listEnabled() {
        // 查询启用的班级
        List<ClassInfo> classList = list(new LambdaQueryWrapper<ClassInfo>()
                .eq(ClassInfo::getStatus, 1)
                .orderByDesc(ClassInfo::getGradeYear)
                .orderByAsc(ClassInfo::getId));

        // 填充班级类型名称
        fillClassTypeName(classList);

        return classList;
    }

    @Override
    public List<ClassInfo> listByClassType(Long classTypeId) {
        List<ClassInfo> classList = list(new LambdaQueryWrapper<ClassInfo>()
                .eq(ClassInfo::getStatus, 1)
                .eq(ClassInfo::getClassTypeId, classTypeId)
                .orderByDesc(ClassInfo::getGradeYear)
                .orderByAsc(ClassInfo::getId));

        fillClassTypeName(classList);

        return classList;
    }

    @Override
    public IPage<ClassInfo> pageClasses(String className, Long classTypeId, Long page, Long pageSize) {
        long current = page == null || page < 1 ? 1 : page;
        long size = pageSize == null || pageSize < 1 ? 10 : pageSize;

        Page<ClassInfo> classPage = new Page<>(current, size);
        IPage<ClassInfo> result = page(classPage, new LambdaQueryWrapper<ClassInfo>()
                .eq(ClassInfo::getStatus, 1)
                .like(className != null && !className.isBlank(), ClassInfo::getClassName, className)
                .eq(classTypeId != null, ClassInfo::getClassTypeId, classTypeId)
                .orderByDesc(ClassInfo::getGradeYear)
                .orderByAsc(ClassInfo::getId));

        fillClassTypeName(result.getRecords());
        return result;
    }

    /**
     * 填充班级类型名称
     *
     * @param classList 班级列表
     */
    private void fillClassTypeName(List<ClassInfo> classList) {
        if (classList.isEmpty()) {
            return;
        }

        // 获取所有班级类型ID
        List<Long> typeIds = classList.stream()
                .map(ClassInfo::getClassTypeId)
                .distinct()
                .collect(Collectors.toList());

        // 查询班级类型
        List<ClassType> typeList = classTypeMapper.selectList(
                new LambdaQueryWrapper<ClassType>()
                        .in(ClassType::getId, typeIds));

        Map<Long, String> typeNameMap = typeList.stream()
                .collect(Collectors.toMap(ClassType::getId, ClassType::getTypeName));

        // 填充类型名称
        classList.forEach(classInfo -> {
            classInfo.setClassTypeName(typeNameMap.get(classInfo.getClassTypeId()));
        });
    }

}