/**
 * 缴费记录管理控制器
 * <p>
 * 提供缴费记录的增删改查接口，包括：
 * - 缴费记录分页查询（支持按学生、学期、费用类型、班级、日期筛选）
 * - 缴费详情查询
 * - 某学生的所有缴费记录查询
 * - 缴费记录新增、修改、删除
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
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

    /** 缴费记录服务 */
    private final PaymentRecordService paymentRecordService;

    /**
     * 分页查询缴费记录
     *
     * @param query 查询条件（学生姓名、学期ID、费用类型ID、班级ID、日期范围、分页参数）
     * @return 缴费记录分页数据
     */
    @GetMapping("/page")
    public Result<PageResult<PaymentRecord>> page(PaymentPageQuery query) {
        IPage<PaymentRecord> page = paymentRecordService.pagePayments(query);
        return Result.success(PageResult.of(page));
    }

    /**
     * 查询缴费详情
     *
     * @param id 缴费记录ID
     * @return 缴费详细信息
     */
    @GetMapping("/{id}")
    public Result<PaymentRecord> detail(@PathVariable Long id) {
        return Result.success(paymentRecordService.getById(id));
    }

    /**
     * 查询某学生的缴费记录
     *
     * @param studentId 学生ID
     * @param query     分页参数
     * @return 该学生的缴费记录分页数据
     */
    @GetMapping("/student/{studentId}")
    public Result<PageResult<PaymentRecord>> listByStudent(@PathVariable Long studentId,
                                                           PaymentPageQuery query) {
        query.setStudentId(studentId);
        IPage<PaymentRecord> page = paymentRecordService.pagePayments(query);
        return Result.success(PageResult.of(page));
    }

    /**
     * 新增缴费记录
     *
     * @param record 缴费记录信息
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result<Void> add(@RequestBody @Valid PaymentRecord record) {
        paymentRecordService.save(record);
        return Result.success();
    }

    /**
     * 修改缴费记录
     *
     * @param record 缴费记录信息
     * @return 操作结果
     */
    @PutMapping("/update")
    public Result<Void> update(@RequestBody @Valid PaymentRecord record) {
        paymentRecordService.updateById(record);
        return Result.success();
    }

    /**
     * 删除缴费记录
     *
     * @param id 缴费记录ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        paymentRecordService.removeById(id);
        return Result.success();
    }
}