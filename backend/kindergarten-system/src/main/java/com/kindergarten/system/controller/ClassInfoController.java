package com.kindergarten.system.controller;

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

    private final ClassInfoService classInfoService;

    @GetMapping("/list")
    public Result<List<ClassInfo>> list() {
        return Result.success(classInfoService.listEnabled());
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody @Valid ClassInfo classInfo) {
        classInfoService.save(classInfo);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody @Valid ClassInfo classInfo) {
        classInfoService.updateById(classInfo);
        return Result.success();
    }

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