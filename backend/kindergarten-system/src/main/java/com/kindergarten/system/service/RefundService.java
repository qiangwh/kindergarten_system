package com.kindergarten.system.service;

import com.kindergarten.system.dto.RefundCalculateResult;

public interface RefundService {

    /**
     * 计算学生某学期的退费金额
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @return 退费计算结果
     */
    RefundCalculateResult calculateRefund(Long studentId, Long semesterId);

}
