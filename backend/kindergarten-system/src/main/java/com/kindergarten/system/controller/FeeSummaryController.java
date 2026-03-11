package com.kindergarten.system.controller;

import com.kindergarten.system.common.result.Result;
import com.kindergarten.system.dto.ClassFeeSummaryItem;
import com.kindergarten.system.dto.ClassFeeTypeSummaryItem;
import com.kindergarten.system.dto.SemesterFeeCompareItem;
import com.kindergarten.system.dto.SemesterFeeSummary;
import com.kindergarten.system.service.FeeSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/fee/summary")
@RequiredArgsConstructor
public class FeeSummaryController {

    private final FeeSummaryService feeSummaryService;

    @GetMapping("/semester/{semesterId}")
    public Result<SemesterFeeSummary> semesterSummary(@PathVariable Long semesterId) {
        return Result.success(feeSummaryService.semesterSummary(semesterId));
    }

    @GetMapping("/compare")
    public Result<List<SemesterFeeCompareItem>> compare(@RequestParam("semesterIds") String semesterIds) {
        List<Long> ids = Arrays.stream(semesterIds.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .filter(item -> item.matches("\\d+"))
                .map(Long::valueOf)
                .collect(Collectors.toList());
        return Result.success(feeSummaryService.semesterCompare(ids));
    }

    @GetMapping("/byClass")
    public Result<List<ClassFeeSummaryItem>> byClass(@RequestParam("semesterId") Long semesterId) {
        return Result.success(feeSummaryService.summaryByClass(semesterId));
    }

    @GetMapping("/byClassAndFeeType")
    public Result<List<ClassFeeTypeSummaryItem>> byClassAndFeeType(@RequestParam("semesterId") Long semesterId) {
        return Result.success(feeSummaryService.summaryByClassAndFeeType(semesterId));
    }
}