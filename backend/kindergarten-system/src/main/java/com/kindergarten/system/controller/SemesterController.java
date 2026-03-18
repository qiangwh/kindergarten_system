package com.kindergarten.system.controller;

import com.kindergarten.system.common.result.PageResult;
import com.kindergarten.system.common.result.Result;
import com.kindergarten.system.entity.Semester;
import com.kindergarten.system.service.SemesterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学期管理控制器
 * <p>
 * 提供学期的增删改查与设置当前学期接口。
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
@RestController
@RequestMapping("/semester")
@RequiredArgsConstructor
public class SemesterController {

    /** 学期服务 */
    private final SemesterService semesterService;

    /**
     * 学期列表
     *
     * @param page         页码（可选）
     * @param pageSize     每页条数（可选）
     * @param semesterName 学期名称（可选，模糊查询）
     * @return 学期列表
     */
    @GetMapping("/list")
    public Result<?> list(@RequestParam(required = false) Long page,
                          @RequestParam(required = false) Long pageSize,
                          @RequestParam(required = false) String semesterName) {
        if (page != null || pageSize != null || (semesterName != null && !semesterName.isBlank())) {
            return Result.success(PageResult.of(semesterService.pageList(semesterName, page, pageSize)));
        }
        return Result.success(semesterService.list());
    }

    /**
     * 新增学期
     *
     * @param semester 学期信息
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result<Void> add(@RequestBody @Valid Semester semester) {
        semesterService.save(semester);
        return Result.success();
    }

    /**
     * 修改学期
     *
     * @param semester 学期信息
     * @return 操作结果
     */
    @PutMapping("/update")
    public Result<Void> update(@RequestBody @Valid Semester semester) {
        semesterService.updateById(semester);
        return Result.success();
    }

    /**
     * 删除学期（软删除）
     *
     * @param id 学期ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Semester semester = semesterService.getById(id);
        if (semester != null) {
            semester.setStatus(0);
            semesterService.updateById(semester);
        }
        return Result.success();
    }

    /**
     * 设置当前学期
     *
     * @param id 学期ID
     * @return 操作结果
     */
    @PutMapping("/setCurrent/{id}")
    public Result<Void> setCurrent(@PathVariable Long id) {
        semesterService.setCurrent(id);
        return Result.success();
    }
}