package com.kindergarten.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kindergarten.system.dto.RefundRuleConfigQuery;
import com.kindergarten.system.entity.RefundRuleConfig;
import com.kindergarten.system.mapper.RefundRuleConfigMapper;
import com.kindergarten.system.service.RefundRuleConfigService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 退费规则配置服务实现类
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
@Service
@CacheConfig(cacheNames = "refundRuleConfig")
public class RefundRuleConfigServiceImpl extends ServiceImpl<RefundRuleConfigMapper, RefundRuleConfig>
        implements RefundRuleConfigService {

    @Override
    @Cacheable(key = "T(com.kindergarten.system.common.cache.CacheKeyUtil).key(#query.ruleType, #query.feeTypeCode, #query.semesterId, #query.page, #query.pageSize)")
    public IPage<RefundRuleConfig> pageList(RefundRuleConfigQuery query) {
        if (query.getPage() == null || query.getPage() < 1) {
            query.setPage(1L);
        }
        if (query.getPageSize() == null || query.getPageSize() < 1) {
            query.setPageSize(20L);
        }

        Page<RefundRuleConfig> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<RefundRuleConfig> wrapper = new LambdaQueryWrapper<>();

        // 按规则类型筛选
        if (StringUtils.hasText(query.getRuleType())) {
            wrapper.eq(RefundRuleConfig::getRuleType, query.getRuleType());
        }

        // 按费用类型编码筛选
        if (StringUtils.hasText(query.getFeeTypeCode())) {
            wrapper.eq(RefundRuleConfig::getFeeTypeCode, query.getFeeTypeCode());
        }

        // 按学期ID筛选
        if (query.getSemesterId() != null) {
            wrapper.eq(RefundRuleConfig::getSemesterId, query.getSemesterId());
        }

        // 默认查询启用的记录
        wrapper.eq(RefundRuleConfig::getStatus, 1);
        wrapper.orderByDesc(RefundRuleConfig::getId);

        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    @Cacheable(key = "'enabled'")
    public List<RefundRuleConfig> listEnabled() {
        LambdaQueryWrapper<RefundRuleConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RefundRuleConfig::getStatus, 1);
        wrapper.orderByDesc(RefundRuleConfig::getId);
        return baseMapper.selectList(wrapper);
    }

    @Override
    @Cacheable(key = "T(com.kindergarten.system.common.cache.CacheKeyUtil).key('id', #id)")
    public RefundRuleConfig getById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    @CacheEvict(allEntries = true)
    public boolean saveConfig(RefundRuleConfig config) {
        // 默认启用状态
        if (config.getStatus() == null) {
            config.setStatus(1);
        }
        return baseMapper.insert(config) > 0;
    }

    @Override
    @CacheEvict(allEntries = true)
    public boolean updateConfig(RefundRuleConfig config) {
        return baseMapper.updateById(config) > 0;
    }

    @Override
    @CacheEvict(allEntries = true)
    public void delete(Long id) {
        RefundRuleConfig config = baseMapper.selectById(id);
        if (config != null) {
            config.setStatus(0);
            baseMapper.updateById(config);
        }
    }
}
