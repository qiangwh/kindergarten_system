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
            String[] titles = {"姓名", "性别", "出生日期", "班级名称", "家长姓名", "家长电话", "入园日期", "离园日期", "状态", "备注"};
            for (int i = 0; i < titles.length; i++) {
                header.createCell(i).setCellValue(titles[i]);
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
                    String name = getStringCellValue(row.getCell(0));
                    if (name == null || name.isBlank()) {
                        throw new IllegalArgumentException("姓名不能为空");
                    }
                    student.setName(name);
                    student.setGender(getStringCellValue(row.getCell(1)));
                    student.setBirthday(getDateCellValue(row.getCell(2)));
                    String className = getStringCellValue(row.getCell(3));
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
                    student.setParentName(getStringCellValue(row.getCell(4)));
                    student.setParentPhone(getStringCellValue(row.getCell(5)));
                    student.setEnrollDate(getDateCellValue(row.getCell(6)));
                    student.setLeaveDate(getDateCellValue(row.getCell(7)));
                    String status = getStringCellValue(row.getCell(8));
                    student.setStatus((status == null || status.isBlank()) ? "active" : status);
                    student.setRemark(getStringCellValue(row.getCell(9)));

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
        cell.setCellType(CellType.STRING);
        String value = cell.getStringCellValue();
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
}