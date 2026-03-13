package com.kindergarten.system.service;

import com.kindergarten.system.dto.RefundCalculateResult;

/**
 * 退费服务接口
 * <p>
 * 支持两种退费类型：
 * - 请假退费（LEAVE）：基于连续请假天数计算
 * - 离园退费（DROPOUT）：基于学期中途离园计算
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
public interface RefundService {

    /**
     * 计算学生某学期的请假退费金额
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @param startDate  统计开始日期（可选）
     * @param endDate    统计结束日期（可选）
     * @return 退费计算结果
     */
    RefundCalculateResult calculateRefund(Long studentId, Long semesterId, String startDate, String endDate);

    /**
     * 计算并保存请假退费记录
     * <p>
     * 计算退费金额后，将结果保存到 refund_record 表
     * </p>
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @return 退费计算结果
     */
    RefundCalculateResult calculateAndSaveRefund(Long studentId, Long semesterId);

    /**
     * 获取已保存的请假退费记录
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @return 退费计算结果（从数据库读取）
     */
    RefundCalculateResult getSavedRefund(Long studentId, Long semesterId);

    /**
     * 计算学生学期中途离园退费金额
     * <p>
     * 当学生在学期中途离园时，根据实际在园天数计算退费金额。
     * 退费金额 = (学期总天数 - 实际在园天数) × 日均费用
     * </p>
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @return 离园退费计算结果
     */
    RefundCalculateResult calculateDropoutRefund(Long studentId, Long semesterId);

    /**
     * 计算并保存离园退费记录
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @return 离园退费计算结果
     */
    RefundCalculateResult calculateAndSaveDropoutRefund(Long studentId, Long semesterId);

    /**
     * 获取已保存的离园退费记录
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @return 离园退费计算结果（从数据库读取）
     */
    RefundCalculateResult getSavedDropoutRefund(Long studentId, Long semesterId);

}
