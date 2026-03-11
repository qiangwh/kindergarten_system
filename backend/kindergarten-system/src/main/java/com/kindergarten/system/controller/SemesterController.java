package com.kindergarten.system.controller;

import com.kindergarten.system.common.result.Result;
import com.kindergarten.system.entity.Semester;
import com.kindergarten.system.service.SemesterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/semester")
@RequiredArgsConstructor
public class SemesterController {

    private final SemesterService semesterService;

    @GetMapping("/list")
    public Result<List<Semester>> list() {
        return Result.success(semesterService.list());
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody @Valid Semester semester) {
        semesterService.save(semester);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody @Valid Semester semester) {
        semesterService.updateById(semester);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Semester semester = semesterService.getById(id);
        if (semester != null) {
            semester.setStatus(0);
            semesterService.updateById(semester);
        }
        return Result.success();
    }

    @PutMapping("/setCurrent/{id}")
    public Result<Void> setCurrent(@PathVariable Long id) {
        semesterService.setCurrent(id);
        return Result.success();
    }
}