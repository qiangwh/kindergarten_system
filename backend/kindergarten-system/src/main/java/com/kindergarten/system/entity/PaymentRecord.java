/**
 * 缴费记录实体类
 * <p>
 * 对应数据库表：payment_record
 * 存储学生的缴费信息，包括缴费金额、缴费日期、收据信息等
 * 支持多张收据图片上传和收据状态管理
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
package com.kindergarten.system.entity;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.kindergarten.system.dto.ReceiptInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 缴费记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "payment_record", autoResultMap = true)
public class PaymentRecord extends BaseEntity {

    /** 学生ID */
    private Long studentId;

    /** 学期ID */
    private Long semesterId;

    /** 费用类型ID */
    private Long feeTypeId;

    /** 缴费金额 */
    private BigDecimal amount;

    /** 缴费日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate payDate;

    /** 收据编号 */
    private String receiptNo;

    /** 收据图片列表（JSON格式存储） */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<ReceiptInfo> receiptImages;

    /** 收据状态：0-无收据，1-待审核，2-已审核 */
    private Integer receiptStatus;

    /** 备注 */
    private String remark;

    // ==================== 非数据库字段 ====================

    /** 学生姓名（非数据库字段，用于展示） */
    @TableField(exist = false)
    private String studentName;

    /** 班级ID（非数据库字段，用于查询） */
    @TableField(exist = false)
    private Long classId;

    /** 班级名称（非数据库字段，用于展示） */
    @TableField(exist = false)
    private String className;

    /** 学期名称（非数据库字段，用于展示） */
    @TableField(exist = false)
    private String semesterName;

    /** 费用类型名称（非数据库字段，用于展示） */
    @TableField(exist = false)
    private String feeTypeName;

    /** 费用类型编码（非数据库字段，用于展示） */
    @TableField(exist = false)
    private String typeCode;

    // ==================== 便捷方法 ====================

    /**
     * 添加收据图片
     *
     * @param receiptInfo 收据信息
     */
    public void addReceipt(ReceiptInfo receiptInfo) {
        if (this.receiptImages == null) {
            this.receiptImages = new ArrayList<>();
        }
        this.receiptImages.add(receiptInfo);
        updateReceiptStatus();
    }

    /**
     * 添加收据图片（简化版）
     *
     * @param url      收据图片URL
     * @param fileName 原始文件名
     */
    public void addReceipt(String url, String fileName) {
        addReceipt(new ReceiptInfo(url, fileName));
    }

    /**
     * 移除收据图片
     *
     * @param url 收据图片URL
     */
    public void removeReceipt(String url) {
        if (this.receiptImages != null) {
            this.receiptImages.removeIf(r -> r.getUrl().equals(url));
            updateReceiptStatus();
        }
    }

    /**
     * 清空所有收据图片
     */
    public void clearReceipts() {
        if (this.receiptImages != null) {
            this.receiptImages.clear();
        }
        this.receiptStatus = 0;
    }

    /**
     * 获取收据数量
     *
     * @return 收据数量
     */
    public int getReceiptCount() {
        return this.receiptImages == null ? 0 : this.receiptImages.size();
    }

    /**
     * 更新收据状态
     */
    private void updateReceiptStatus() {
        if (this.receiptImages == null || this.receiptImages.isEmpty()) {
            this.receiptStatus = 0;
        } else if (this.receiptStatus == null || this.receiptStatus == 0) {
            this.receiptStatus = 1; // 有收据则设为待审核
        }
    }

}