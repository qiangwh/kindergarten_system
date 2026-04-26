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
import com.kindergarten.system.dto.DashboardOverview;
import com.kindergarten.system.entity.ClassInfo;
import com.kindergarten.system.entity.ClassType;
import com.kindergarten.system.mapper.ClassInfoMapper;
import com.kindergarten.system.mapper.StudentMapper;
import com.kindergarten.system.service.ClassInfoService;
import com.kindergarten.system.service.ClassTypeService;
import com.kindergarten.system.common.cache.CacheKeyUtil;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.io.Serializable;
import java.util.stream.Collectors;

@Service
@CacheConfig(cacheNames = "classInfo")
@RequiredArgsConstructor
public class ClassInfoServiceImpl extends ServiceImpl<ClassInfoMapper, ClassInfo> implements ClassInfoService {

    /** 班级类型服务 */
    private final ClassTypeService classTypeService;

    /** 学生 Mapper（用于实时统计班级学生数） */
    private final StudentMapper studentMapper;

    @Override
    @Cacheable(key = "'enabled'")
    public List<ClassInfo> listEnabled() {
        // 查询启用的班级
        List<ClassInfo> classList = list(new LambdaQueryWrapper<ClassInfo>()
                .eq(ClassInfo::getStatus, 1)
                .orderByDesc(ClassInfo::getGradeYear)
                .orderByAsc(ClassInfo::getId));

        // 填充班级类型名称和学生人数
        fillClassTypeName(classList);
        fillStudentCount(classList);

        return classList;
    }

    @Override
    @Cacheable(key = "T(com.kindergarten.system.common.cache.CacheKeyUtil).key('classType', #classTypeId)")
    public List<ClassInfo> listByClassType(Long classTypeId) {
        List<ClassInfo> classList = list(new LambdaQueryWrapper<ClassInfo>()
                .eq(ClassInfo::getStatus, 1)
                .eq(ClassInfo::getClassTypeId, classTypeId)
                .orderByDesc(ClassInfo::getGradeYear)
                .orderByAsc(ClassInfo::getId));

        fillClassTypeName(classList);
        fillStudentCount(classList);

        return classList;
    }

    @Override
    @Cacheable(key = "T(com.kindergarten.system.common.cache.CacheKeyUtil).key(#className, #classTypeId, #page, #pageSize)")
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
        fillStudentCount(result.getRecords());
        return result;
    }

    @Override
    @CacheEvict(cacheNames = {"classInfo", "dashboard", "feeSummary"}, allEntries = true)
    public boolean save(ClassInfo entity) {
        return super.save(entity);
    }

    @Override
    @CacheEvict(cacheNames = {"classInfo", "dashboard", "feeSummary"}, allEntries = true)
    public boolean updateById(ClassInfo entity) {
        return super.updateById(entity);
    }

    @Override
    @CacheEvict(cacheNames = {"classInfo", "dashboard", "feeSummary"}, allEntries = true)
    public boolean removeById(Serializable id) {
        return super.removeById(id);
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
        List<ClassType> typeList = classTypeService.list();

        Map<Long, String> typeNameMap = typeList.stream()
                .filter(type -> typeIds.contains(type.getId()))
                .collect(Collectors.toMap(ClassType::getId, ClassType::getTypeName));

        // 填充类型名称
        classList.forEach(classInfo -> {
            classInfo.setClassTypeName(typeNameMap.get(classInfo.getClassTypeId()));
        });
    }

    /**
     * 实时填充班级的在读学生人数
     *
     * @param classList 班级列表
     */
    private void fillStudentCount(List<ClassInfo> classList) {
        if (classList.isEmpty()) {
            return;
        }

        // 通过已有的 SQL 查询各班级在读学生数
        List<DashboardOverview.ClassStudentCount> counts = studentMapper.selectActiveStudentCountsByClass();

        // 构建 classId -> count 映射
        Map<Long, Long> countMap = counts.stream()
                .collect(Collectors.toMap(
                        DashboardOverview.ClassStudentCount::getClassId,
                        DashboardOverview.ClassStudentCount::getCount,
                        (a, b) -> a
                ));

        // 填充到班级对象中
        classList.forEach(classInfo -> {
            Long count = countMap.get(classInfo.getId());
            classInfo.setCurrentCount(count != null ? count.intValue() : 0);
        });
    }

}
