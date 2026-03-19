package com.kindergarten.system.service.impl;

import com.kindergarten.system.dto.ClassFeeSummaryItem;
import com.kindergarten.system.dto.ClassFeeTypeSummaryItem;
import com.kindergarten.system.dto.FeeTypeSummaryItem;
import com.kindergarten.system.dto.SemesterFeeCompareItem;
import com.kindergarten.system.dto.SemesterFeeSummary;
import com.kindergarten.system.mapper.FeeSummaryMapper;
import com.kindergarten.system.service.FeeSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
@CacheConfig(cacheNames = "feeSummary")
@RequiredArgsConstructor
public class FeeSummaryServiceImpl implements FeeSummaryService {

    private final FeeSummaryMapper feeSummaryMapper;

    /**
     * 学期汇总数据缓存。
     * <p>
     * 学期汇总在首页和汇总页会被反复使用，而其结果只会在缴费、退费、学期切换等操作后变化，
     * 因此适合直接缓存聚合结果。
     * </p>
     */
    @Override
    @Cacheable(key = "T(com.kindergarten.system.common.cache.CacheKeyUtil).key('semester', #semesterId)")
    public SemesterFeeSummary semesterSummary(Long semesterId) {
        BigDecimal total = feeSummaryMapper.selectSemesterTotal(semesterId);
        if (total == null) {
            total = BigDecimal.ZERO;
        }
        List<FeeTypeSummaryItem> byFeeType = feeSummaryMapper.selectSemesterByFeeType(semesterId);
        if (byFeeType == null) {
            byFeeType = Collections.emptyList();
        }
        SemesterFeeSummary summary = new SemesterFeeSummary();
        summary.setSemesterId(semesterId);
        summary.setTotalAmount(total);
        summary.setByFeeType(byFeeType);
        return summary;
    }

    /**
     * 学期对比数据缓存。
     * <p>
     * 对比结果完全由入参决定，查询条件不变时无需重复扫描数据库。
     * </p>
     */
    @Override
    @Cacheable(key = "T(com.kindergarten.system.common.cache.CacheKeyUtil).key('compare', #semesterIds)")
    public List<SemesterFeeCompareItem> semesterCompare(List<Long> semesterIds) {
        if (semesterIds == null || semesterIds.isEmpty()) {
            return Collections.emptyList();
        }
        return feeSummaryMapper.selectSemesterCompare(semesterIds);
    }

    /**
     * 按班级汇总缓存。
     * <p>
     * 这个接口用于汇总统计页面，适合缓存整份查询结果，减少班级维度聚合开销。
     * </p>
     */
    @Override
    @Cacheable(key = "T(com.kindergarten.system.common.cache.CacheKeyUtil).key('byClass', #semesterId)")
    public List<ClassFeeSummaryItem> summaryByClass(Long semesterId) {
        List<ClassFeeSummaryItem> items = feeSummaryMapper.selectByClass(semesterId);
        if (items == null) {
            return Collections.emptyList();
        }
        return items;
    }

    /**
     * 按班级 + 收费项汇总缓存。
     * <p>
     * 该查询通常为表格展示使用，命中缓存后可以显著降低重复汇总成本。
     * </p>
     */
    @Override
    @Cacheable(key = "T(com.kindergarten.system.common.cache.CacheKeyUtil).key('byClassAndFeeType', #semesterId)")
    public List<ClassFeeTypeSummaryItem> summaryByClassAndFeeType(Long semesterId) {
        List<ClassFeeTypeSummaryItem> items = feeSummaryMapper.selectByClassAndFeeType(semesterId);
        if (items == null) {
            return Collections.emptyList();
        }
        return items;
    }
}