package com.kindergarten.system.controller;

import com.kindergarten.system.common.result.Result;
import com.kindergarten.system.entity.ClassType;
import com.kindergarten.system.service.ClassTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 班级类型管理控制器
 * <p>
 * 提供班级类型的增删改查接口，班级类型包括：小班、中班、大班
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
@RestController
@RequestMapping("/classType")
@RequiredArgsConstructor
public class ClassTypeController {

    /** 班级类型服务 */
    private final ClassTypeService classTypeService;

    /**
     * 班级类型列表
     *
     * @return 所有启用的班级类型
     */
    @GetMapping("/list")
    public Result<List<ClassType>> list() {
        return Result.success(classTypeService.listEnabled());
    }

    /**
     * 新增班级类型
     *
     * @param classType 班级类型信息
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result<Void> add(@RequestBody @Valid ClassType classType) {
        classTypeService.save(classType);
        return Result.success();
    }

    /**
     * 修改班级类型
     *
     * @param classType 班级类型信息
     * @return 操作结果
     */
    @PutMapping("/update")
    public Result<Void> update(@RequestBody @Valid ClassType classType) {
        classTypeService.updateById(classType);
        return Result.success();
    }

    /**
     * 删除班级类型（软删除）
     *
     * @param id 班级类型ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        ClassType classType = classTypeService.getById(id);
        if (classType != null) {
            classType.setStatus(0);
            classTypeService.updateById(classType);
        }
        return Result.success();
    }

}
