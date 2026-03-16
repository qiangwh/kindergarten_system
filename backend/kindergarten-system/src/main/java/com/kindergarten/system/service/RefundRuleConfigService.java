package com.kindergarten.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kindergarten.system.dto.RefundRuleConfigQuery;
import com.kindergarten.system.entity.RefundRuleConfig;

import java.util.List;

/**
 * 退费规则配置服务接口
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
public interface RefundRuleConfigService {

    /**
     * 分页查询退费规则配置列表
     *
     * @param query 查询参数
     * @return 分页结果
     */
    IPage<RefundRuleConfig> pageList(RefundRuleConfigQuery query);

    /**
     * 查询所有启用的退费规则配置
     *
     * @return 规则配置列表
     */
    List<RefundRuleConfig> listEnabled();

    /**
     * 根据ID查询退费规则配置
     *
     * @param id 规则配置ID
     * @return 规则配置信息
     */
    RefundRuleConfig getById(Long id);

    /**
     * 新增退费规则配置
     *
     * @param config 规则配置信息
     */
    boolean saveConfig(RefundRuleConfig config);

    /**
     * 修改退费规则配置
     *
     * @param config 规则配置信息
     */
    boolean updateConfig(RefundRuleConfig config);

    /**
     * 删除退费规则配置（软删除）
     *
     * @param id 规则配置ID
     */
    void delete(Long id);
}
