package com.kindergarten.system.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 考勤Excel导入结果
 * <p>
 * 返回导入的统计信息和错误详情。
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
@Data
public class AttendanceImportResult {

    /** 总行数 */
    private int totalRows;

    /** 成功导入数 */
    private int successCount;

    /** 失败数 */
    private int failCount;

    /** 错误详情列表 */
    private List<ImportError> errors = new ArrayList<>();

    /**
     * 导入错误详情
     */
    @Data
    public static class ImportError {

        /** 行号（Excel中的行号，从1开始） */
        private int rowNum;

        /** 学生姓名 */
        private String studentName;

        /** 错误信息 */
        private String message;

        public ImportError(int rowNum, String studentName, String message) {
            this.rowNum = rowNum;
            this.studentName = studentName;
            this.message = message;
        }
    }

    /**
     * 添加错误信息
     */
    public void addError(int rowNum, String studentName, String message) {
        this.errors.add(new ImportError(rowNum, studentName, message));
    }

    /**
     * 是否全部成功
     */
    public boolean isAllSuccess() {
        return failCount == 0;
    }
}
