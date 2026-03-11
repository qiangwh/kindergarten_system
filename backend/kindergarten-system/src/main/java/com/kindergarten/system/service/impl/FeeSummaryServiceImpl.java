package com.kindergarten.system.service.impl;

import com.kindergarten.system.dto.ClassFeeSummaryItem;
import com.kindergarten.system.dto.ClassFeeTypeSummaryItem;
import com.kindergarten.system.dto.FeeTypeSummaryItem;
import com.kindergarten.system.dto.SemesterFeeCompareItem;
import com.kindergarten.system.dto.SemesterFeeSummary;
import com.kindergarten.system.mapper.FeeSummaryMapper;
import com.kindergarten.system.service.FeeSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeeSummaryServiceImpl implements FeeSummaryService {

    private final FeeSummaryMapper feeSummaryMapper;

    @Override
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

    @Override
    public List<SemesterFeeCompareItem> semesterCompare(List<Long> semesterIds) {
        if (semesterIds == null || semesterIds.isEmpty()) {
            return Collections.emptyList();
        }
        return feeSummaryMapper.selectSemesterCompare(semesterIds);
    }

    @Override
    public List<ClassFeeSummaryItem> summaryByClass(Long semesterId) {
        List<ClassFeeSummaryItem> items = feeSummaryMapper.selectByClass(semesterId);
        if (items == null) {
            return Collections.emptyList();
        }
        return items;
    }

    @Override
    public List<ClassFeeTypeSummaryItem> summaryByClassAndFeeType(Long semesterId) {
        List<ClassFeeTypeSummaryItem> items = feeSummaryMapper.selectByClassAndFeeType(semesterId);
        if (items == null) {
            return Collections.emptyList();
        }
        return items;
    }
}