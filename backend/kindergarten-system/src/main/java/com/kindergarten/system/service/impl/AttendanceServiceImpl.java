package com.kindergarten.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kindergarten.system.dto.*;
import com.kindergarten.system.entity.Attendance;
import com.kindergarten.system.entity.ClassInfo;
import com.kindergarten.system.entity.Student;
import com.kindergarten.system.mapper.AttendanceMapper;
import com.kindergarten.system.mapper.ClassInfoMapper;
import com.kindergarten.system.mapper.StudentMapper;
import com.kindergarten.system.service.AttendanceService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 考勤服务实现
 * <p>
 * 提供考勤记录的查询、录入、统计等功能实现。
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl extends ServiceImpl<AttendanceMapper, Attendance> implements AttendanceService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATE_DISPLAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final StudentMapper studentMapper;
    private final ClassInfoMapper classInfoMapper;

    @Override
    public List<Attendance> listByDate(AttendanceListQuery query) {
        return baseMapper.selectAttendanceList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBatchAttendance(AttendanceSaveRequest request) {
        LocalDate attendDate = request.getAttendDate();
        List<AttendanceSaveItem> items = request.getItems();
        if (items == null || items.isEmpty()) {
            return;
        }

        List<Long> studentIds = items.stream()
                .map(AttendanceSaveItem::getStudentId)
                .distinct()
                .toList();

        // 删除已有记录
        remove(new LambdaQueryWrapper<Attendance>()
                .eq(Attendance::getAttendDate, attendDate)
                .in(Attendance::getStudentId, studentIds));

        // 批量插入新记录
        List<Attendance> attendanceList = items.stream()
                .map(item -> {
                    Attendance attendance = new Attendance();
                    attendance.setClassId(request.getClassId());
                    attendance.setStudentId(item.getStudentId());
                    attendance.setAttendDate(attendDate);
                    attendance.setStatus(item.getStatus());
                    attendance.setRemark(item.getRemark());
                    return attendance;
                })
                .toList();
        saveBatch(attendanceList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMultiDayAttendance(AttendanceBatchRequest request) {
        Long classId = request.getClassId();
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        List<AttendanceBatchRequest.AttendanceBatchItem> items = request.getItems();

        if (items == null || items.isEmpty() || startDate == null || endDate == null) {
            return;
        }

        // 验证日期范围
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }

        // 获取日期范围内的所有日期
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            dates.add(current);
            current = current.plusDays(1);
        }

        // 获取所有学生ID
        List<Long> studentIds = items.stream()
                .map(AttendanceBatchRequest.AttendanceBatchItem::getStudentId)
                .distinct()
                .toList();

        // 删除日期范围内的已有记录
        remove(new LambdaQueryWrapper<Attendance>()
                .eq(Attendance::getClassId, classId)
                .in(Attendance::getStudentId, studentIds)
                .ge(Attendance::getAttendDate, startDate)
                .le(Attendance::getAttendDate, endDate));

        // 批量插入新记录
        List<Attendance> attendanceList = new ArrayList<>();
        Map<Long, AttendanceBatchRequest.AttendanceBatchItem> itemMap = items.stream()
                .collect(Collectors.toMap(
                        AttendanceBatchRequest.AttendanceBatchItem::getStudentId,
                        Function.identity(),
                        (left, right) -> right
                ));

        for (LocalDate date : dates) {
            for (Long studentId : studentIds) {
                AttendanceBatchRequest.AttendanceBatchItem item = itemMap.get(studentId);
                if (item != null) {
                    Attendance attendance = new Attendance();
                    attendance.setClassId(classId);
                    attendance.setStudentId(studentId);
                    attendance.setAttendDate(date);
                    attendance.setStatus(item.getStatus());
                    attendance.setRemark(item.getRemark());
                    attendanceList.add(attendance);
                }
            }
        }

        // 分批插入（每批500条）
        int batchSize = 500;
        for (int i = 0; i < attendanceList.size(); i += batchSize) {
            int end = Math.min(i + batchSize, attendanceList.size());
            saveBatch(attendanceList.subList(i, end));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveGroupAttendance(AttendanceGroupRequest request) {
        Long classId = request.getClassId();
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        String status = request.getStatus();
        Boolean excludeWeekends = request.getExcludeWeekends();
        String remark = request.getRemark();

        // 获取学生列表
        List<Student> students;
        if (request.getStudentIds() != null && !request.getStudentIds().isEmpty()) {
            students = studentMapper.selectBatchIds(request.getStudentIds());
        } else {
            // 获取班级所有在读学生
            students = studentMapper.selectList(
                    new LambdaQueryWrapper<Student>()
                            .eq(Student::getClassId, classId)
                            .eq(Student::getStatus, "active"));
        }

        if (students.isEmpty()) {
            return;
        }

        List<Long> studentIds = students.stream()
                .map(Student::getId)
                .toList();

        // 获取日期范围
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            // 排除周末
            if (Boolean.TRUE.equals(excludeWeekends)) {
                DayOfWeek dayOfWeek = current.getDayOfWeek();
                if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                    dates.add(current);
                }
            } else {
                dates.add(current);
            }
            current = current.plusDays(1);
        }

        // 删除已有记录
        remove(new LambdaQueryWrapper<Attendance>()
                .eq(Attendance::getClassId, classId)
                .in(Attendance::getStudentId, studentIds)
                .in(Attendance::getAttendDate, dates));

        // 批量插入
        List<Attendance> attendanceList = new ArrayList<>();
        for (LocalDate date : dates) {
            for (Long studentId : studentIds) {
                Attendance attendance = new Attendance();
                attendance.setClassId(classId);
                attendance.setStudentId(studentId);
                attendance.setAttendDate(date);
                attendance.setStatus(status);
                attendance.setRemark(remark);
                attendanceList.add(attendance);
            }
        }

        // 分批插入
        int batchSize = 500;
        for (int i = 0; i < attendanceList.size(); i += batchSize) {
            int end = Math.min(i + batchSize, attendanceList.size());
            saveBatch(attendanceList.subList(i, end));
        }
    }

    @Override
    public void downloadTemplate(Long classId, String startDate, String endDate, HttpServletResponse response) throws IOException {
        // 获取班级信息
        ClassInfo classInfo = classInfoMapper.selectById(classId);
        String className = classInfo != null ? classInfo.getClassName() : "班级";

        // 获取班级学生
        List<Student> students = studentMapper.selectList(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getClassId, classId)
                        .eq(Student::getStatus, "active")
                        .orderByAsc(Student::getId));

        // 解析日期范围
        LocalDate start = LocalDate.parse(startDate, DATE_FORMAT);
        LocalDate end = LocalDate.parse(endDate, DATE_FORMAT);

        // 生成日期列表
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = start;
        while (!current.isAfter(end)) {
            dates.add(current);
            current = current.plusDays(1);
        }

        // 创建Excel工作簿
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("考勤录入");

            // 创建样式
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // 创建表头
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("学生ID");
            headerRow.createCell(1).setCellValue("学生姓名");
            headerRow.createCell(2).setCellValue("性别");

            // 设置日期列表头
            for (int i = 0; i < dates.size(); i++) {
                Cell cell = headerRow.createCell(3 + i);
                cell.setCellValue(dates.get(i).format(DATE_DISPLAY));
                cell.setCellStyle(headerStyle);
            }

            // 设置表头样式
            for (int i = 0; i < 3; i++) {
                headerRow.getCell(i).setCellStyle(headerStyle);
            }

            // 填充学生数据
            int rowNum = 1;
            for (Student student : students) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(student.getId());
                row.createCell(1).setCellValue(student.getName());
                row.createCell(2).setCellValue("M".equals(student.getGender()) ? "男" : "女");
                // 日期列留空，等待用户填写
                for (int i = 0; i < dates.size(); i++) {
                    row.createCell(3 + i);
                }
            }

            // 添加说明行
            Row noteRow = sheet.createRow(rowNum + 1);
            Cell noteCell = noteRow.createCell(0);
            noteCell.setCellValue("说明：考勤状态填写 present（出勤）、absent（缺勤）、leave（请假），留空默认为出勤");

            // 设置响应头
            String fileName = URLEncoder.encode(className + "-考勤录入模板.xlsx", StandardCharsets.UTF_8);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

            // 写入响应
            try (OutputStream os = response.getOutputStream()) {
                workbook.write(os);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttendanceImportResult importFromExcel(Long classId, Long semesterId, MultipartFile file) throws IOException {
        AttendanceImportResult result = new AttendanceImportResult();

        // 获取班级学生映射
        List<Student> students = studentMapper.selectList(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getClassId, classId)
                        .eq(Student::getStatus, "active"));
        Map<String, Student> studentMap = students.stream()
                .collect(Collectors.toMap(Student::getName, Function.identity(), (a, b) -> a));

        // 解析Excel
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // 获取表头日期
            Row headerRow = sheet.getRow(0);
            List<LocalDate> dates = new ArrayList<>();
            for (int i = 3; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null && cell.getCellType() == CellType.STRING) {
                    try {
                        dates.add(LocalDate.parse(cell.getStringCellValue(), DATE_DISPLAY));
                    } catch (Exception e) {
                        // 忽略无效日期
                    }
                }
            }

            // 收集所有考勤记录
            List<Attendance> attendanceList = new ArrayList<>();

            // 遍历数据行
            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null) continue;

                result.setTotalRows(result.getTotalRows() + 1);

                // 获取学生信息
                Cell nameCell = row.getCell(1);
                if (nameCell == null) continue;

                String studentName = getCellValueAsString(nameCell);
                Student student = studentMap.get(studentName);
                if (student == null) {
                    result.addError(rowNum + 1, studentName, "学生不存在");
                    result.setFailCount(result.getFailCount() + 1);
                    continue;
                }

                // 遍历日期列
                for (int i = 0; i < dates.size(); i++) {
                    Cell cell = row.getCell(3 + i);
                    if (cell == null) continue;

                    String status = getCellValueAsString(cell);
                    if (status == null || status.isEmpty()) {
                        status = "present"; // 默认出勤
                    }

                    // 验证状态值
                    if (!Arrays.asList("present", "absent", "leave").contains(status.toLowerCase())) {
                        result.addError(rowNum + 1, studentName, "无效的考勤状态: " + status);
                        continue;
                    }

                    Attendance attendance = new Attendance();
                    attendance.setClassId(classId);
                    attendance.setStudentId(student.getId());
                    attendance.setAttendDate(dates.get(i));
                    attendance.setStatus(status.toLowerCase());
                    attendanceList.add(attendance);
                }

                result.setSuccessCount(result.getSuccessCount() + 1);
            }

            // 删除已有记录后批量插入
            if (!attendanceList.isEmpty()) {
                // 按学生ID分组删除
                List<Long> studentIds = attendanceList.stream()
                        .map(Attendance::getStudentId)
                        .distinct()
                        .toList();
                List<LocalDate> dateList = dates;

                remove(new LambdaQueryWrapper<Attendance>()
                        .eq(Attendance::getClassId, classId)
                        .in(Attendance::getStudentId, studentIds)
                        .in(Attendance::getAttendDate, dateList));

                // 分批插入
                int batchSize = 500;
                for (int i = 0; i < attendanceList.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, attendanceList.size());
                    saveBatch(attendanceList.subList(i, end));
                }
            }
        }

        return result;
    }

    /**
     * 获取单元格字符串值
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    @Override
    public List<AttendanceStudentStatItem> statByStudent(Long classId, String startDate, String endDate) {
        return baseMapper.selectStudentStats(classId, startDate, endDate);
    }

    @Override
    public AttendanceMonthStatItem statByMonth(Long classId) {
        LocalDate now = LocalDate.now();
        LocalDate start = now.withDayOfMonth(1);
        LocalDate end = now.withDayOfMonth(now.lengthOfMonth());
        AttendanceMonthStatItem summary = baseMapper.selectMonthSummary(classId, start.format(DATE_FORMAT), end.format(DATE_FORMAT));
        if (summary == null) {
            summary = new AttendanceMonthStatItem();
            summary.setClassId(classId);
            summary.setMonth(now.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            summary.setPresentDays(0);
            summary.setLeaveDays(0);
            summary.setAbsentDays(0);
            summary.setTotalDays(0);
        } else if (summary.getMonth() == null) {
            summary.setMonth(now.format(DateTimeFormatter.ofPattern("yyyy-MM")));
        }
        return summary;
    }
}