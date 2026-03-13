package com.kindergarten.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kindergarten.system.common.exception.BusinessException;
import com.kindergarten.system.common.result.ResultCode;
import com.kindergarten.system.dto.StudentExportQuery;
import com.kindergarten.system.dto.StudentImportResult;
import com.kindergarten.system.dto.StudentPageQuery;
import com.kindergarten.system.entity.ClassInfo;
import com.kindergarten.system.entity.Student;
import com.kindergarten.system.mapper.ClassInfoMapper;
import com.kindergarten.system.mapper.StudentMapper;
import com.kindergarten.system.service.StudentService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;

@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ClassInfoMapper classInfoMapper;

    public StudentServiceImpl(ClassInfoMapper classInfoMapper) {
        this.classInfoMapper = classInfoMapper;
    }

    @Override
    public IPage<Student> pageStudents(StudentPageQuery query) {
        if (query.getPage() == null || query.getPage() < 1) {
            query.setPage(1L);
        }
        if (query.getPageSize() == null || query.getPageSize() < 1) {
            query.setPageSize(10L);
        }
        Page<Student> page = new Page<>(query.getPage(), query.getPageSize());
        return baseMapper.selectStudentPage(page, query);
    }

    @Override
    public void updateStatus(Long id, String status) {
        Student student = getById(id);
        if (student == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        student.setStatus(status);
        // 如果状态变为离园，且离园日期为空，则设置离园日期为今天
        if ("inactive".equals(status) && student.getLeaveDate() == null) {
            student.setLeaveDate(LocalDate.now());
        }
        // 如果状态复园（active），清空离园日期
        if ("active".equals(status)) {
            student.setLeaveDate(null);
        }
        updateById(student);
    }

    @Override
    public List<Student> listStudentsForExport(StudentExportQuery query) {
        return baseMapper.selectStudentsForExport(query);
    }

    @Override
    public void exportStudents(StudentExportQuery query, OutputStream outputStream) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("学生信息");
            Row header = sheet.createRow(0);
            String[] titles = {"学号", "姓名", "性别", "出生日期", "班级名称", "家长姓名", "家长电话", "入园日期", "离园日期", "状态", "备注"};
            for (int i = 0; i < titles.length; i++) {
                header.createCell(i).setCellValue(titles[i]);
            }

            List<Student> students = listStudentsForExport(query);
            if (students != null && !students.isEmpty()) {
                int rowIndex = 1;
                for (Student student : students) {
                    Row row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(nullToEmpty(student.getStudentNo()));
                    row.createCell(1).setCellValue(nullToEmpty(student.getName()));
                    row.createCell(2).setCellValue(formatGender(student.getGender()));
                    row.createCell(3).setCellValue(formatDate(student.getBirthday()));
                    row.createCell(4).setCellValue(nullToEmpty(student.getClassName()));
                    row.createCell(5).setCellValue(nullToEmpty(student.getParentName()));
                    row.createCell(6).setCellValue(nullToEmpty(student.getParentPhone()));
                    row.createCell(7).setCellValue(formatDate(student.getEnrollDate()));
                    row.createCell(8).setCellValue(formatDate(student.getLeaveDate()));
                    row.createCell(9).setCellValue(formatStatus(student.getStatus()));
                    row.createCell(10).setCellValue(nullToEmpty(student.getRemark()));
                }
            }

            for (int i = 0; i < titles.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
        }
    }

    @Override
    public StudentImportResult importStudents(MultipartFile file) throws IOException {
        StudentImportResult result = new StudentImportResult();
        List<Student> students = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getLastRowNum() < 1) {
                return result;
            }

            int lastRow = sheet.getLastRowNum();
            for (int i = 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                result.setTotalCount(result.getTotalCount() + 1);
                try {
                    Student student = new Student();
                    String studentNo = getStringCellValue(row.getCell(0));
                    String name = getStringCellValue(row.getCell(1));
                    if (name == null || name.isBlank()) {
                        throw new IllegalArgumentException("姓名不能为空");
                    }
                    student.setName(name);
                    student.setGender(parseGender(getStringCellValue(row.getCell(2))));
                    student.setBirthday(getDateCellValue(row.getCell(3)));
                    String className = getStringCellValue(row.getCell(4));
                    if (className == null || className.isBlank()) {
                        throw new IllegalArgumentException("班级名称不能为空");
                    }
                    ClassInfo classInfo = classInfoMapper.selectOne(new LambdaQueryWrapper<ClassInfo>()
                            .eq(ClassInfo::getClassName, className)
                            .eq(ClassInfo::getStatus, 1)
                            .last("limit 1"));
                    if (classInfo == null) {
                        throw new IllegalArgumentException("班级名称不存在：" + className);
                    }
                    student.setClassId(classInfo.getId());
                    student.setParentName(getStringCellValue(row.getCell(5)));
                    student.setParentPhone(getStringCellValue(row.getCell(6)));
                    student.setEnrollDate(getDateCellValue(row.getCell(7)));
                    student.setLeaveDate(getDateCellValue(row.getCell(8)));
                    String status = getStringCellValue(row.getCell(9));
                    student.setStatus(parseStatus(status));
                    student.setRemark(getStringCellValue(row.getCell(10)));
                    
                    // 如果学号为空，自动生成学号
                    if (studentNo == null || studentNo.isBlank()) {
                        studentNo = generateStudentNo(classInfo, student.getEnrollDate());
                    }
                    student.setStudentNo(studentNo);

                    students.add(student);
                    result.setSuccessCount(result.getSuccessCount() + 1);
                } catch (Exception ex) {
                    result.setFailCount(result.getFailCount() + 1);
                    result.getErrorMessages().add("第" + (i + 1) + "行导入失败：" + ex.getMessage());
                }
            }
        }

        if (!students.isEmpty()) {
            saveBatch(students);
        }

        return result;
    }

    private String formatDate(LocalDate date) {
        return date == null ? "" : DATE_FORMATTER.format(date);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            double numericValue = cell.getNumericCellValue();
            if (DateUtil.isCellDateFormatted(cell)) {
                return DATE_FORMATTER.format(cell.getLocalDateTimeCellValue().toLocalDate());
            }
            long longValue = Math.round(numericValue);
            if (Math.abs(numericValue - longValue) < 0.00001) {
                return String.valueOf(longValue);
            }
            return String.valueOf(numericValue);
        }
        // 使用DataFormatter来获取单元格字符串值，避免弃用方法
        String value;
        if (cell.getCellType() == CellType.STRING) {
            value = cell.getStringCellValue();
        } else {
            // 对于其他类型，使用DataFormatter格式化
            org.apache.poi.ss.usermodel.DataFormatter formatter = new org.apache.poi.ss.usermodel.DataFormatter();
            value = formatter.formatCellValue(cell);
        }
        return value == null ? null : value.trim();
    }

    private LocalDate getDateCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        String value = getStringCellValue(cell);
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value, DATE_FORMATTER);
    }

    private String formatGender(String gender) {
        if (gender == null) {
            return "";
        }
        if ("M".equals(gender)) {
            return "男";
        }
        if ("F".equals(gender)) {
            return "女";
        }
        return gender;
    }

    private String formatStatus(String status) {
        if (status == null) {
            return "";
        }
        if ("active".equals(status)) {
            return "在读";
        }
        if ("inactive".equals(status)) {
            return "离园";
        }
        return status;
    }

    private String parseGender(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        if ("男".equals(value) || "M".equals(normalized)) {
            return "M";
        }
        if ("女".equals(value) || "F".equals(normalized)) {
            return "F";
        }
        return null;
    }

    private String parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return "active";
        }
        String normalized = value.trim().toLowerCase();
        if ("在读".equals(value) || "active".equals(normalized)) {
            return "active";
        }
        if ("离园".equals(value) || "inactive".equals(normalized)) {
            return "inactive";
        }
        return "active";
    }

    /**
     * 生成学号
     * 格式：入园年份-班级代码-序号（如：2024-XB1-001）
     */
    private String generateStudentNo(ClassInfo classInfo, LocalDate enrollDate) {
        // 获取入园年份
        int year;
        if (enrollDate != null) {
            year = enrollDate.getYear();
        } else {
            year = LocalDate.now().getYear();
        }
        
        // 从班级名称提取班级代码
        String classCode = extractClassCode(classInfo.getClassName());
        
        // 查询该班级当前最大序号
        String prefix = year + "-" + classCode + "-";
        Integer maxSeq = baseMapper.selectMaxStudentNoSequence(prefix);
        int nextSeq = (maxSeq != null ? maxSeq : 0) + 1;
        
        return String.format("%d-%s-%03d", year, classCode, nextSeq);
    }
    
    /**
     * 从班级名称提取班级代码
     * 如："小班1班" -> "XB1"，"中班2班" -> "ZB2"，"大班3班" -> "DB3"
     */
    private String extractClassCode(String className) {
        if (className == null || className.isBlank()) {
            return "XX";
        }
        
        // 提取班级类型和序号
        Pattern pattern = Pattern.compile("(小|中|大)班(\\d+)班");
        Matcher matcher = pattern.matcher(className);
        
        if (matcher.find()) {
            String type = matcher.group(1);
            String number = matcher.group(2);
            
            String typeCode;
            switch (type) {
                case "小":
                    typeCode = "XB";
                    break;
                case "中":
                    typeCode = "ZB";
                    break;
                case "大":
                    typeCode = "DB";
                    break;
                default:
                    typeCode = "XX";
            }
            
            return typeCode + number;
        }
        
        // 如果无法匹配，返回班级名称的前4个字符（大写）
        String code = className.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        return code.length() > 4 ? code.substring(0, 4) : code;
    }
}