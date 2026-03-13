package com.kindergarten.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kindergarten.system.dto.PaymentPageQuery;
import com.kindergarten.system.entity.PaymentRecord;

import java.util.List;

/**
 * 缴费记录服务接口
 */
public interface PaymentRecordService extends IService<PaymentRecord> {

    /**
     * 分页查询缴费记录
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<PaymentRecord> pagePayments(PaymentPageQuery query);

    /**
     * 批量更新收据状态
     *
     * @param ids    缴费记录ID列表
     * @param status 收据状态
     */
    void batchUpdateReceiptStatus(List<Long> ids, Integer status);
}