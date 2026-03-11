package com.kindergarten.system.controller;

import com.kindergarten.system.common.result.Result;
import com.kindergarten.system.entity.FeeType;
import com.kindergarten.system.service.FeeTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/feeType")
@RequiredArgsConstructor
public class FeeTypeController {

    private final FeeTypeService feeTypeService;

    @GetMapping("/list")
    public Result<List<FeeType>> list() {
        return Result.success(feeTypeService.listEnabled());
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody @Valid FeeType feeType) {
        feeTypeService.save(feeType);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody @Valid FeeType feeType) {
        feeTypeService.updateById(feeType);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        FeeType feeType = feeTypeService.getById(id);
        if (feeType != null) {
            feeType.setStatus(0);
            feeTypeService.updateById(feeType);
        }
        return Result.success();
    }
}