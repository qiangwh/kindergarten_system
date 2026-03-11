package com.kindergarten.system.service;

import com.kindergarten.system.dto.ClassFeeSummaryItem;
import com.kindergarten.system.dto.ClassFeeTypeSummaryItem;
import com.kindergarten.system.dto.SemesterFeeCompareItem;
import com.kindergarten.system.dto.SemesterFeeSummary;

import java.util.List;

public interface FeeSummaryService {

    SemesterFeeSummary semesterSummary(Long semesterId);

    List<SemesterFeeCompareItem> semesterCompare(List<Long> semesterIds);

    List<ClassFeeSummaryItem> summaryByClass(Long semesterId);

    List<ClassFeeTypeSummaryItem> summaryByClassAndFeeType(Long semesterId);
}