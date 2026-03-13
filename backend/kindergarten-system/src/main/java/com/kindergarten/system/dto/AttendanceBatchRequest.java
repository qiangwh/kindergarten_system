package com.kindergarten.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 批量考勤录入请求
 * <p>
 * 支持一次为多个学生录入多天的考勤状态，提高录入效率。
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
@Data
public class AttendanceBatchRequest {

    /** 班级ID */
    @NotNull(message = "班级ID不能为空")
    private Long classId;

    /** 开始日期 */
    @NotNull(message = "开始日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /** 结束日期 */
    @NotNull(message = "结束日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /** 考勤记录项（学生+状态） */
    @Valid
    @NotEmpty(message = "考勤记录不能为空")
    private List<AttendanceBatchItem> items;

    /**
     * 批量考勤项
     */
    @Data
    public static class AttendanceBatchItem {

        /** 学生ID */
        @NotNull(message = "学生ID不能为空")
        private Long studentId;

        /** 考勤状态：present-出勤，absent-缺勤，leave-请假 */
        @NotNull(message = "考勤状态不能为空")
        private String status;

        /** 备注 */
        private String remark;
    }
}
