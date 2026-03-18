/**
 * 班级管理控制器
 * <p>
 * 提供班级的增删改查接口，班级关联班级类型（小班、中班、大班）
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
package com.kindergarten.system.controller;

import com.kindergarten.system.common.result.PageResult;
import com.kindergarten.system.common.result.Result;
import com.kindergarten.system.entity.ClassInfo;
import com.kindergarten.system.service.ClassInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/class")
@RequiredArgsConstructor
public class ClassInfoController {

    /** 班级服务 */
    private final ClassInfoService classInfoService;

    /**
     * 班级列表
     *
     * @param page        页码（可选）
     * @param pageSize    每页条数（可选）
     * @param className   班级名称（可选，模糊查询）
     * @param classTypeId 班级类型ID（可选）
     * @return 班级列表
     */
    @GetMapping("/list")
    public Result<?> list(@RequestParam(required = false) Long page,
                          @RequestParam(required = false) Long pageSize,
                          @RequestParam(required = false) String className,
                          @RequestParam(required = false) Long classTypeId) {
        if (page != null || pageSize != null || (className != null && !className.isBlank())) {
            return Result.success(PageResult.of(classInfoService.pageClasses(className, classTypeId, page, pageSize)));
        }
        if (classTypeId != null) {
            return Result.success(classInfoService.listByClassType(classTypeId));
        }
        return Result.success(classInfoService.listEnabled());
    }

    /**
     * 新增班级
     *
     * @param classInfo 班级信息
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result<Void> add(@RequestBody @Valid ClassInfo classInfo) {
        // 初始化当前学生数为0
        classInfo.setCurrentCount(0);
        classInfoService.save(classInfo);
        return Result.success();
    }

    /**
     * 修改班级
     *
     * @param classInfo 班级信息
     * @return 操作结果
     */
    @PutMapping("/update")
    public Result<Void> update(@RequestBody @Valid ClassInfo classInfo) {
        classInfoService.updateById(classInfo);
        return Result.success();
    }

    /**
     * 删除班级（软删除）
     *
     * @param id 班级ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        ClassInfo classInfo = classInfoService.getById(id);
        if (classInfo != null) {
            classInfo.setStatus(0);
            classInfoService.updateById(classInfo);
        }
        return Result.success();
    }

}