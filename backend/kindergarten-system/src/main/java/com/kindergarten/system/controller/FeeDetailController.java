package com.kindergarten.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kindergarten.system.common.result.PageResult;
import com.kindergarten.system.common.result.Result;
import com.kindergarten.system.dto.FeeDetailQuery;
import com.kindergarten.system.dto.StudentFeeDetail;
import com.kindergarten.system.service.FeeDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 收费明细管理控制器
 * <p>
 * 提供收费明细查询接口，展示每个学生在当前学期的详细费用信息：
 * - 学生基本信息
 * - 各项费用明细（学费、餐费等）
 * - 退费金额
 * - 考勤统计
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
@RestController
@RequestMapping("/fee/detail")
@RequiredArgsConstructor
public class FeeDetailController {

    /** 收费明细服务 */
    private final FeeDetailService feeDetailService;

    /**
     * 分页查询学生收费明细
     *
     * @param query 查询参数（semesterId必填，classId和studentName可选）
     * @return 学生收费明细分页数据
     */
    @GetMapping("/page")
    public Result<PageResult<StudentFeeDetail>> page(FeeDetailQuery query) {
        if (query.getSemesterId() == null) {
            return Result.error(10001, "学期ID不能为空");
        }
        IPage<StudentFeeDetail> page = feeDetailService.pageStudentFeeDetails(query);
        return Result.success(PageResult.of(page));
    }

    /**
     * 获取单个学生的收费明细
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @return 学生收费明细
     */
    @GetMapping("/student/{studentId}")
    public Result<StudentFeeDetail> getStudentDetail(
            @PathVariable Long studentId,
            @RequestParam Long semesterId) {
        StudentFeeDetail detail = feeDetailService.getStudentFeeDetail(studentId, semesterId);
        if (detail == null) {
            return Result.error(10002, "学生不存在");
        }
        return Result.success(detail);
    }

    /**
     * 获取学期收费明细汇总（按班级）
     *
     * @param semesterId 学期ID
     * @return 各班级收费汇总
     */
    @GetMapping("/class-summary")
    public Result<List<FeeDetailService.ClassFeeSummary>> getClassSummary(
            @RequestParam Long semesterId) {
        return Result.success(feeDetailService.getClassFeeSummary(semesterId));
    }

    /**
     * 导出单个学生收费明细PDF
     *
     * @param studentId 学生ID
     * @param semesterId 学期ID
     * @return PDF文件
     */
    @GetMapping("/student/{studentId}/export")
    public ResponseEntity<byte[]> exportStudentDetailPdf(
            @PathVariable Long studentId,
            @RequestParam Long semesterId) throws IOException {
        StudentFeeDetail detail = feeDetailService.getStudentFeeDetail(studentId, semesterId);
        if (detail == null) {
            return ResponseEntity.notFound().build();
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        feeDetailService.exportStudentFeeDetailPdf(studentId, semesterId, outputStream);
        byte[] bytes = outputStream.toByteArray();

        String fileName = String.format("%s-收费明细.pdf", detail.getStudentName());
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        String contentDisposition = "attachment; filename*=UTF-8''" + encoded;

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentLength(bytes.length)
                .body(bytes);
    }

}
