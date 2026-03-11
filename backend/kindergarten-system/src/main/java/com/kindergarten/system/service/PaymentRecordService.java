package com.kindergarten.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kindergarten.system.dto.PaymentPageQuery;
import com.kindergarten.system.entity.PaymentRecord;

public interface PaymentRecordService extends IService<PaymentRecord> {

    IPage<PaymentRecord> pagePayments(PaymentPageQuery query);
}