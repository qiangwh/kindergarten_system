package com.kindergarten.system.service;

import com.kindergarten.system.dto.RefundCalculateResult;

/**
 * 退费服务接口
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
public interface RefundService {

    /**
     * 计算学生某学期的退费金额
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @param startDate  统计开始日期（可选）
     * @param endDate    统计结束日期（可选）
     * @return 退费计算结果
     */
    RefundCalculateResult calculateRefund(Long studentId, Long semesterId, String startDate, String endDate);

    /**
     * 计算并保存退费记录
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
     * 获取已保存的退费记录
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @return 退费计算结果（从数据库读取）
     */
    RefundCalculateResult getSavedRefund(Long studentId, Long semesterId);

}
