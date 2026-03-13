/**
 * 退费计算控制器
 * <p>
 * 提供退费计算相关接口，根据学生考勤记录计算应退金额：
 * - 连续请假 5-9 天：退伙食费（12.7元/天）
 * - 连续请假 10天及以上：退保教费（66.3元/天）+ 伙食费（61.7元/天）
 * - 不足 5 天：不退费
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
package com.kindergarten.system.controller;

import com.kindergarten.system.common.result.Result;
import com.kindergarten.system.dto.RefundCalculateResult;
import com.kindergarten.system.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 退费计算控制器
 */
@RestController
@RequestMapping("/fee/refund")
@RequiredArgsConstructor
public class RefundController {

    /** 退费服务 */
    private final RefundService refundService;

    /**
     * 计算退费金额（仅计算，不保存）
     *
     * @param studentId 学生ID
     * @param semesterId 学期ID
     * @param startDate 统计开始日期（可选）
     * @param endDate 统计结束日期（可选）
     * @return 退费计算结果
     */
    @GetMapping("/calculate")
    public Result<RefundCalculateResult> calculate(
            @RequestParam Long studentId,
            @RequestParam Long semesterId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(refundService.calculateRefund(studentId, semesterId, startDate, endDate));
    }

    /**
     * 计算并保存退费记录
     * <p>
     * 计算退费金额后，将结果保存到 refund_record 表
     * </p>
     *
     * @param studentId 学生ID
     * @param semesterId 学期ID
     * @return 退费计算结果
     */
    @PostMapping("/save")
    public Result<RefundCalculateResult> calculateAndSave(
            @RequestParam Long studentId,
            @RequestParam Long semesterId) {
        return Result.success(refundService.calculateAndSaveRefund(studentId, semesterId));
    }

    /**
     * 获取已保存的退费记录
     *
     * @param studentId 学生ID
     * @param semesterId 学期ID
     * @return 退费计算结果（从数据库读取）
     */
    @GetMapping("/saved")
    public Result<RefundCalculateResult> getSaved(
            @RequestParam Long studentId,
            @RequestParam Long semesterId) {
        return Result.success(refundService.getSavedRefund(studentId, semesterId));
    }

}
