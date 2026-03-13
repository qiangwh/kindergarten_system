package com.kindergarten.system.controller;

import com.kindergarten.system.common.result.Result;
import com.kindergarten.system.dto.*;
import com.kindergarten.system.entity.Attendance;
import com.kindergarten.system.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 考勤管理控制器
 * <p>
 * 提供考勤记录查询、保存及统计相关接口。
 * 支持单日录入、多天批量录入、群组操作、Excel导入导出等功能。
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
@Tag(name = "考勤管理", description = "考勤记录查询、录入、统计、Excel导入导出")
public class AttendanceController {

    /** 考勤服务 */
    private final AttendanceService attendanceService;

    /**
     * 查询考勤列表
     *
     * @param query 查询条件（班级ID、日期范围等）
     * @return 考勤列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询考勤列表", description = "按班级和日期查询考勤记录")
    public Result<List<Attendance>> list(AttendanceListQuery query) {
        return Result.success(attendanceService.listByDate(query));
    }

    /**
     * 保存单日考勤记录
     * <p>
     * 为指定日期的多个学生批量保存考勤状态。
     * </p>
     *
     * @param request 保存请求
     * @return 操作结果
     */
    @PostMapping("/save")
    @Operation(summary = "保存单日考勤", description = "为指定日期的多个学生批量保存考勤状态")
    public Result<Void> save(@RequestBody @Valid AttendanceSaveRequest request) {
        attendanceService.saveBatchAttendance(request);
        return Result.success();
    }

    /**
     * 批量保存多天考勤
     * <p>
     * 支持一次为多个学生录入连续多天的考勤状态，提高录入效率。
     * </p>
     *
     * @param request 批量保存请求
     * @return 操作结果
     */
    @PostMapping("/batch")
    @Operation(summary = "多天批量录入", description = "一次为多个学生录入连续多天的考勤状态")
    public Result<Void> saveBatch(@RequestBody @Valid AttendanceBatchRequest request) {
        attendanceService.saveMultiDayAttendance(request);
        return Result.success();
    }

    /**
     * 群组考勤设置
     * <p>
     * 对整个班级或选定学生群体进行统一考勤设置，可排除周末。
     * 适用于全员出勤、集体请假等场景。
     * </p>
     *
     * @param request 群组设置请求
     * @return 操作结果
     */
    @PostMapping("/group")
    @Operation(summary = "群组考勤设置", description = "对整个班级或选定学生群体进行统一考勤设置，可排除周末")
    public Result<Void> saveGroup(@RequestBody @Valid AttendanceGroupRequest request) {
        attendanceService.saveGroupAttendance(request);
        return Result.success();
    }

    /**
     * 下载考勤导入模板
     * <p>
     * 生成包含班级学生和指定日期范围的Excel模板，供批量导入使用。
     * </p>
     *
     * @param classId   班级ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param response  HTTP响应
     */
    @GetMapping("/template")
    @Operation(summary = "下载Excel模板", description = "生成包含班级学生和指定日期范围的Excel模板")
    public void downloadTemplate(
            @Parameter(description = "班级ID") @RequestParam Long classId,
            @Parameter(description = "开始日期") @RequestParam String startDate,
            @Parameter(description = "结束日期") @RequestParam String endDate,
            HttpServletResponse response) throws IOException {
        attendanceService.downloadTemplate(classId, startDate, endDate, response);
    }

    /**
     * 导入考勤Excel
     * <p>
     * 解析上传的Excel文件，批量导入考勤数据。
     * 返回导入结果，包括成功数、失败数和错误详情。
     * </p>
     *
     * @param classId    班级ID
     * @param semesterId 学期ID
     * @param file       Excel文件
     * @return 导入结果
     */
    @PostMapping("/import")
    @Operation(summary = "Excel批量导入", description = "解析上传的Excel文件，批量导入考勤数据")
    public Result<AttendanceImportResult> importExcel(
            @Parameter(description = "班级ID") @RequestParam Long classId,
            @Parameter(description = "学期ID") @RequestParam Long semesterId,
            @Parameter(description = "Excel文件") @RequestParam("file") MultipartFile file) throws IOException {
        return Result.success(attendanceService.importFromExcel(classId, semesterId, file));
    }

    /**
     * 按学生统计考勤
     *
     * @param classId   班级ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 学生考勤统计列表
     */
    @GetMapping("/statByStudent")
    @Operation(summary = "按学生统计", description = "按学生维度统计考勤数据")
    public Result<List<AttendanceStudentStatItem>> statByStudent(
            @Parameter(description = "班级ID") @RequestParam Long classId,
            @Parameter(description = "开始日期") @RequestParam String startDate,
            @Parameter(description = "结束日期") @RequestParam String endDate) {
        return Result.success(attendanceService.statByStudent(classId, startDate, endDate));
    }

    /**
     * 按月份统计考勤
     *
     * @param classId 班级ID
     * @return 班级月度考勤统计
     */
    @GetMapping("/statByMonth")
    @Operation(summary = "按月份统计", description = "按月份统计班级考勤数据")
    public Result<AttendanceMonthStatItem> statByMonth(
            @Parameter(description = "班级ID") @RequestParam Long classId) {
        return Result.success(attendanceService.statByMonth(classId));
    }
}