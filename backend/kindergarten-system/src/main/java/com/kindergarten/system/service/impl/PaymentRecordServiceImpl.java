package com.kindergarten.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kindergarten.system.dto.PaymentPageQuery;
import com.kindergarten.system.entity.PaymentRecord;
import com.kindergarten.system.mapper.PaymentRecordMapper;
import com.kindergarten.system.service.PaymentRecordService;
import org.springframework.stereotype.Service;

@Service
public class PaymentRecordServiceImpl extends ServiceImpl<PaymentRecordMapper, PaymentRecord>
        implements PaymentRecordService {

    @Override
    public IPage<PaymentRecord> pagePayments(PaymentPageQuery query) {
        if (query.getPage() == null || query.getPage() < 1) {
            query.setPage(1L);
        }
        if (query.getPageSize() == null || query.getPageSize() < 1) {
            query.setPageSize(10L);
        }
        Page<PaymentRecord> page = new Page<>(query.getPage(), query.getPageSize());
        return baseMapper.selectPaymentPage(page, query);
    }
}