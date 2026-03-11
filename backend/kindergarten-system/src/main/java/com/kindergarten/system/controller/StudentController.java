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

    private final StudentService studentService;

    @GetMapping("/page")
    public Result<PageResult<Student>> page(StudentPageQuery query) {
        IPage<Student> page = studentService.pageStudents(query);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/{id}")
    public Result<Student> detail(@PathVariable Long id) {
        return Result.success(studentService.getById(id));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody @Valid Student student) {
        studentService.save(student);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody @Valid Student student) {
        studentService.updateById(student);
        return Result.success();
    }

    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        studentService.updateStatus(id, status);
        return Result.success();
    }

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