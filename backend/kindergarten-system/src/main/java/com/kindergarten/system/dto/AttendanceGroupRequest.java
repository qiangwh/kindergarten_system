package com.kindergarten.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 群组考勤设置请求
 * <p>
 * 支持对整个班级或选定学生群体进行统一考勤设置，快速完成批量录入。
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
@Data
public class AttendanceGroupRequest {

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

    /**
     * 指定学生ID列表
     * <p>
     * 如果为空，则对班级所有学生生效
     * </p>
     */
    private List<Long> studentIds;

    /** 统一设置的考勤状态：present-出勤，absent-缺勤，leave-请假 */
    @NotBlank(message = "考勤状态不能为空")
    private String status;

    /** 是否排除周末（周六、周日不录入） */
    private Boolean excludeWeekends = true;

    /** 备注 */
    private String remark;
}
