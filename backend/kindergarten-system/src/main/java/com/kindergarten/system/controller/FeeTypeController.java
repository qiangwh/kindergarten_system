package com.kindergarten.system.controller;

import com.kindergarten.system.common.result.PageResult;
import com.kindergarten.system.common.result.Result;
import com.kindergarten.system.entity.FeeType;
import com.kindergarten.system.service.FeeTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收费类型管理控制器
 * <p>
 * 提供收费类型的增删改查接口。
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
@RestController
@RequestMapping("/feeType")
@RequiredArgsConstructor
public class FeeTypeController {

    /** 收费类型服务 */
    private final FeeTypeService feeTypeService;

    /**
     * 查询收费类型列表
     *
     * @param page     页码（可选）
     * @param pageSize 每页条数（可选）
     * @param typeName 费用类型名称（可选，模糊查询）
     * @return 收费类型列表
     */
    @GetMapping("/list")
    public Result<?> list(@RequestParam(required = false) Long page,
                          @RequestParam(required = false) Long pageSize,
                          @RequestParam(required = false) String typeName) {
        if (page != null || pageSize != null || (typeName != null && !typeName.isBlank())) {
            return Result.success(PageResult.of(feeTypeService.pageList(typeName, page, pageSize)));
        }
        return Result.success(feeTypeService.listEnabled());
    }

    /**
     * 新增收费类型
     *
     * @param feeType 收费类型信息
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result<Void> add(@RequestBody @Valid FeeType feeType) {
        feeTypeService.save(feeType);
        return Result.success();
    }

    /**
     * 修改收费类型
     *
     * @param feeType 收费类型信息
     * @return 操作结果
     */
    @PutMapping("/update")
    public Result<Void> update(@RequestBody @Valid FeeType feeType) {
        feeTypeService.updateById(feeType);
        return Result.success();
    }

    /**
     * 删除收费类型（软删除）
     *
     * @param id 收费类型ID
     * @return 操作结果
     */
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