package com.kindergarten.system.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 缴费记录分页查询参数
 */
@Data
public class PaymentPageQuery {

    /** 页码 */
    private Long page = 1L;
    
    /** 每页条数 */
    private Long pageSize = 10L;
    
    /** 学生姓名（模糊查询） */
    private String studentName;
    
    /** 学生ID */
    private Long studentId;
    
    /** 学期ID */
    private Long semesterId;
    
    /** 费用类型ID */
    private Long feeTypeId;
    
    /** 班级ID */
    private Long classId;
    
    /** 缴费开始日期 */
    private LocalDate payDateFrom;
    
    /** 缴费结束日期 */
    private LocalDate payDateTo;
    
    /** 收据状态：0-无收据，1-待审核，2-已审核 */
    private Integer receiptStatus;
}