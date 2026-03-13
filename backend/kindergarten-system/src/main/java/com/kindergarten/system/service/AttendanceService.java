package com.kindergarten.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kindergarten.system.dto.*;
import com.kindergarten.system.entity.Attendance;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 考勤服务接口
 * <p>
 * 提供考勤记录的查询、录入、统计等功能。
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
public interface AttendanceService extends IService<Attendance> {

    /**
     * 按日期查询考勤列表
     *
     * @param query 查询条件
     * @return 考勤列表
     */
    List<Attendance> listByDate(AttendanceListQuery query);

    /**
     * 保存单日考勤（批量学生）
     *
     * @param request 保存请求
     */
    void saveBatchAttendance(AttendanceSaveRequest request);

    /**
     * 批量保存考勤（多天多学生）
     * <p>
     * 支持一次为多个学生录入连续多天的考勤状态。
     * </p>
     *
     * @param request 批量保存请求
     */
    void saveMultiDayAttendance(AttendanceBatchRequest request);

    /**
     * 群组考勤设置
     * <p>
     * 对整个班级或选定学生群体进行统一考勤设置，可排除周末。
     * </p>
     *
     * @param request 群组设置请求
     */
    void saveGroupAttendance(AttendanceGroupRequest request);

    /**
     * 下载考勤导入模板
     *
     * @param classId   班级ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param response  HTTP响应
     * @throws IOException IO异常
     */
    void downloadTemplate(Long classId, String startDate, String endDate, HttpServletResponse response) throws IOException;

    /**
     * 导入考勤Excel
     *
     * @param classId  班级ID
     * @param semesterId 学期ID
     * @param file     Excel文件
     * @return 导入结果
     * @throws IOException IO异常
     */
    AttendanceImportResult importFromExcel(Long classId, Long semesterId, MultipartFile file) throws IOException;

    /**
     * 按学生统计考勤
     *
     * @param classId   班级ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 学生考勤统计列表
     */
    List<AttendanceStudentStatItem> statByStudent(Long classId, String startDate, String endDate);

    /**
     * 按月份统计考勤
     *
     * @param classId 班级ID
     * @return 班级月度考勤统计
     */
    AttendanceMonthStatItem statByMonth(Long classId);
}