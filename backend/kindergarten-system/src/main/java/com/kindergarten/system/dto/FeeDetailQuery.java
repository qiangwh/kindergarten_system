package com.kindergarten.system.dto;

import lombok.Data;

/**
 * 收费明细查询参数
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
@Data
public class FeeDetailQuery {

    /** 学期ID（必填） */
    private Long semesterId;

    /** 班级ID（可选，筛选特定班级） */
    private Long classId;

    /** 学生姓名（可选，模糊查询） */
    private String studentName;
    /**
     * 是否包含离园学生
     * <p>
     * 默认 false，仅显示在读学生；
     * 设为 true 时，同时显示离园学生（用于查看历史缴费和退费记录）
     * </p>
     */
    private Boolean includeInactive = false;
    /** 页码 */
    private Long page = 1L;

    /** 每页条数 */
    private Long pageSize = 20L;

}
