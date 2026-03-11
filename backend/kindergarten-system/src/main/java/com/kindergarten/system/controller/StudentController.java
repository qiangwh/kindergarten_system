/**
 * 学生管理控制器
 * <p>
 * 提供学生信息的增删改查接口，包括：
 * - 学生分页查询（支持按姓名、班级、状态筛选）
 * - 学生详情查询
 * - 学生信息新增、修改
 * - 学生状态变更（active/inactive）
 * - 学生删除（软删除）
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
package com.kindergarten.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kindergarten.system.common.result.PageResult;
import com.kindergarten.system.common.result.Result;
import com.kindergarten.system.dto.StudentPageQuery;
import com.kindergarten.system.entity.Student;
import com.kindergarten.system.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    /** 学生服务 */
    private final StudentService studentService;

    /**
     * 分页查询学生列表
     *
     * @param query 查询条件（姓名模糊查询、班级ID、状态、分页参数）
     * @return 学生分页数据
     */
    @GetMapping("/page")
    public Result<PageResult<Student>> page(StudentPageQuery query) {
        IPage<Student> page = studentService.pageStudents(query);
        return Result.success(PageResult.of(page));
    }

    /**
     * 查询学生详情
     *
     * @param id 学生ID
     * @return 学生详细信息
     */
    @GetMapping("/{id}")
    public Result<Student> detail(@PathVariable Long id) {
        return Result.success(studentService.getById(id));
    }

    /**
     * 新增学生
     *
     * @param student 学生信息
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result<Void> add(@RequestBody @Valid Student student) {
        studentService.save(student);
        return Result.success();
    }

    /**
     * 修改学生信息
     *
     * @param student 学生信息
     * @return 操作结果
     */
    @PutMapping("/update")
    public Result<Void> update(@RequestBody @Valid Student student) {
        studentService.updateById(student);
        return Result.success();
    }

    /**
     * 修改学生状态
     *
     * @param id     学生ID
     * @param status 状态（active-在读，inactive-离园）
     * @return 操作结果
     */
    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        studentService.updateStatus(id, status);
        return Result.success();
    }

    /**
     * 删除学生（软删除，将状态置为 inactive）
     *
     * @param id 学生ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Student student = studentService.getById(id);
        if (student != null) {
            student.setStatus("inactive");
            studentService.updateById(student);
        }
        return Result.success();
    }
}