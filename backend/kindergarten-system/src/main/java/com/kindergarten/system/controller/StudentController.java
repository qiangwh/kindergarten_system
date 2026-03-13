package com.kindergarten.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kindergarten.system.common.result.PageResult;
import com.kindergarten.system.common.result.Result;
import com.kindergarten.system.common.result.ResultCode;
import com.kindergarten.system.dto.StudentExportQuery;
import com.kindergarten.system.dto.StudentImportResult;
import com.kindergarten.system.dto.StudentPageQuery;
import com.kindergarten.system.entity.Student;
import com.kindergarten.system.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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

    @GetMapping("/export")
    public void exportStudents(StudentExportQuery query, HttpServletResponse response) throws IOException {
        String fileName = URLEncoder.encode("学生信息.xlsx", StandardCharsets.UTF_8.name());
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName);
        studentService.exportStudents(query, response.getOutputStream());
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<StudentImportResult> importStudents(@RequestPart("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return Result.failure(ResultCode.PARAM_ERROR, "导入文件不能为空");
        }
        StudentImportResult result = studentService.importStudents(file);
        return Result.success(result);
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