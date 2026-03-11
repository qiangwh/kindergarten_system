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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fee/refund")
@RequiredArgsConstructor
public class RefundController {

    /** 退费服务 */
    private final RefundService refundService;

    /**
     * 计算退费金额
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @return 退费计算结果
     */
    @GetMapping("/calculate")
    public Result<RefundCalculateResult> calculate(
            @RequestParam Long studentId,
            @RequestParam Long semesterId) {
        return Result.success(refundService.calculateRefund(studentId, semesterId));
    }

}
