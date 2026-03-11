package com.kindergarten.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kindergarten.system.dto.PaymentPageQuery;
import com.kindergarten.system.entity.PaymentRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PaymentRecordMapper extends BaseMapper<PaymentRecord> {

    IPage<PaymentRecord> selectPaymentPage(IPage<PaymentRecord> page, @Param("query") PaymentPageQuery query);
}