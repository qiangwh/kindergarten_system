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

/**
 * 收费汇总控制器
 * <p>
 * 提供学期汇总、学期对比、按班级与按班级费用类型汇总等接口。
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
@RestController
@RequestMapping("/fee/summary")
@RequiredArgsConstructor
public class FeeSummaryController {

    /** 收费汇总服务 */
    private final FeeSummaryService feeSummaryService;

    /**
     * 获取学期收费汇总
     *
     * @param semesterId 学期ID
     * @return 学期收费汇总
     */
    @GetMapping("/semester/{semesterId}")
    public Result<SemesterFeeSummary> semesterSummary(@PathVariable Long semesterId) {
        return Result.success(feeSummaryService.semesterSummary(semesterId));
    }

    /**
     * 学期费用对比
     *
     * @param semesterIds 学期ID列表（逗号分隔）
     * @return 学期费用对比结果
     */
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

    /**
     * 按班级汇总学期费用
     *
     * @param semesterId 学期ID
     * @return 班级收费汇总
     */
    @GetMapping("/byClass")
    public Result<List<ClassFeeSummaryItem>> byClass(@RequestParam("semesterId") Long semesterId) {
        return Result.success(feeSummaryService.summaryByClass(semesterId));
    }

    /**
     * 按班级与费用类型汇总学期费用
     *
     * @param semesterId 学期ID
     * @return 班级费用类型汇总
     */
    @GetMapping("/byClassAndFeeType")
    public Result<List<ClassFeeTypeSummaryItem>> byClassAndFeeType(@RequestParam("semesterId") Long semesterId) {
        return Result.success(feeSummaryService.summaryByClassAndFeeType(semesterId));
    }
}