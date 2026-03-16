package com.kindergarten.system.controller;

import com.kindergarten.system.common.result.PageResult;
import com.kindergarten.system.common.result.Result;
import com.kindergarten.system.dto.RefundRuleConfigQuery;
import com.kindergarten.system.entity.RefundRuleConfig;
import com.kindergarten.system.service.RefundRuleConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 退费规则配置管理控制器
 * <p>
 * 提供退费规则配置的增删改查接口，支持按规则类型、费用类型筛选。
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
@RestController
@RequestMapping("/refund-rule")
@RequiredArgsConstructor
public class RefundRuleConfigController {

    /** 退费规则配置服务 */
    private final RefundRuleConfigService refundRuleConfigService;

    /**
     * 查询退费规则配置列表（分页）
     *
     * @param query 查询参数（支持ruleType、feeTypeCode、semesterId筛选）
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result<PageResult<RefundRuleConfig>> list(RefundRuleConfigQuery query) {
        return Result.success(PageResult.of(refundRuleConfigService.pageList(query)));
    }

    /**
     * 查询所有启用的退费规则配置
     *
     * @return 规则配置列表
     */
    @GetMapping("/all")
    public Result<List<RefundRuleConfig>> all() {
        return Result.success(refundRuleConfigService.listEnabled());
    }

    /**
     * 根据ID查询退费规则配置详情
     *
     * @param id 规则配置ID
     * @return 规则配置详情
     */
    @GetMapping("/{id}")
    public Result<RefundRuleConfig> detail(@PathVariable Long id) {
        return Result.success(refundRuleConfigService.getById(id));
    }

    /**
     * 新增退费规则配置
     *
     * @param config 规则配置信息
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result<Void> add(@RequestBody @Valid RefundRuleConfig config) {
        refundRuleConfigService.saveConfig(config);
        return Result.success();
    }

    /**
     * 修改退费规则配置
     *
     * @param config 规则配置信息
     * @return 操作结果
     */
    @PutMapping("/update")
    public Result<Void> update(@RequestBody @Valid RefundRuleConfig config) {
        refundRuleConfigService.updateConfig(config);
        return Result.success();
    }

    /**
     * 删除退费规则配置（软删除）
     *
     * @param id 规则配置ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        refundRuleConfigService.delete(id);
        return Result.success();
    }
}
