package com.kindergarten.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kindergarten.system.dto.FeeDetailQuery;
import com.kindergarten.system.dto.StudentFeeDetail;

import java.util.List;

/**
 * 收费明细服务接口
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
public interface FeeDetailService {

    /**
     * 分页查询学生收费明细
     *
     * @param query 查询参数
     * @return 学生收费明细分页数据
     */
    IPage<StudentFeeDetail> pageStudentFeeDetails(FeeDetailQuery query);

    /**
     * 获取单个学生的收费明细
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @return 学生收费明细
     */
    StudentFeeDetail getStudentFeeDetail(Long studentId, Long semesterId);

    /**
     * 获取学期收费明细汇总（按班级）
     *
     * @param semesterId 学期ID
     * @return 各班级收费汇总
     */
    List<ClassFeeSummary> getClassFeeSummary(Long semesterId);

    /**
     * 班级收费汇总
     */
    record ClassFeeSummary(
            Long classId,
            String className,
            Integer studentCount,
            java.math.BigDecimal totalPaid,
            java.math.BigDecimal totalRefund,
            java.math.BigDecimal actualAmount
    ) {}

}
