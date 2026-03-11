package com.kindergarten.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kindergarten.system.common.result.PageResult;
import com.kindergarten.system.common.result.Result;
import com.kindergarten.system.dto.PaymentPageQuery;
import com.kindergarten.system.entity.PaymentRecord;
import com.kindergarten.system.service.PaymentRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentRecordController {

    private final PaymentRecordService paymentRecordService;

    @GetMapping("/page")
    public Result<PageResult<PaymentRecord>> page(PaymentPageQuery query) {
        IPage<PaymentRecord> page = paymentRecordService.pagePayments(query);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/{id}")
    public Result<PaymentRecord> detail(@PathVariable Long id) {
        return Result.success(paymentRecordService.getById(id));
    }

    @GetMapping("/student/{studentId}")
    public Result<PageResult<PaymentRecord>> listByStudent(@PathVariable Long studentId,
                                                           PaymentPageQuery query) {
        query.setStudentId(studentId);
        IPage<PaymentRecord> page = paymentRecordService.pagePayments(query);
        return Result.success(PageResult.of(page));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody @Valid PaymentRecord record) {
        paymentRecordService.save(record);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody @Valid PaymentRecord record) {
        paymentRecordService.updateById(record);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        paymentRecordService.removeById(id);
        return Result.success();
    }
}