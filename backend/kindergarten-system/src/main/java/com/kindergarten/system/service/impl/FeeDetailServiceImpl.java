package com.kindergarten.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kindergarten.system.dto.FeeDetailQuery;
import com.kindergarten.system.dto.RefundCalculateResult;
import com.kindergarten.system.dto.RefundCalculateResult.LeaveSegment;
import com.kindergarten.system.dto.RefundCalculateResult.RefundItem;
import com.kindergarten.system.dto.StudentFeeDetail;
import com.kindergarten.system.entity.*;
import com.kindergarten.system.mapper.*;
import com.kindergarten.system.service.FeeDetailService;
import com.kindergarten.system.service.RefundService;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 收费明细服务实现类
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class FeeDetailServiceImpl implements FeeDetailService {

    private final StudentMapper studentMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final AttendanceMapper attendanceMapper;
    private final FeeTypeMapper feeTypeMapper;
    private final SemesterMapper semesterMapper;
    private final ClassInfoMapper classInfoMapper;
    private final RefundService refundService;

    @Override
    public IPage<StudentFeeDetail> pageStudentFeeDetails(FeeDetailQuery query) {
        // 构建学生查询条件
        LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
        // 根据参数决定是否包含离园学生
        if (!Boolean.TRUE.equals(query.getIncludeInactive())) {
            studentWrapper.eq(Student::getStatus, "active");
        }
        
        if (query.getClassId() != null) {
            studentWrapper.eq(Student::getClassId, query.getClassId());
        }
        if (query.getStudentName() != null && !query.getStudentName().isEmpty()) {
            studentWrapper.like(Student::getName, query.getStudentName());
        }
        studentWrapper.orderByAsc(Student::getClassId).orderByAsc(Student::getId);

        // 分页查询学生
        Page<Student> studentPage = new Page<>(query.getPage(), query.getPageSize());
        IPage<Student> students = studentMapper.selectPage(studentPage, studentWrapper);

        // 转换为收费明细
        Page<StudentFeeDetail> resultPage = new Page<>(students.getCurrent(), students.getSize(), students.getTotal());
        List<StudentFeeDetail> details = students.getRecords().stream()
                .map(student -> buildStudentFeeDetail(student, query.getSemesterId()))
                .collect(Collectors.toList());
        resultPage.setRecords(details);

        return resultPage;
    }

    @Override
    public StudentFeeDetail getStudentFeeDetail(Long studentId, Long semesterId) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            return null;
        }
        return buildStudentFeeDetail(student, semesterId);
    }

    @Override
    public List<ClassFeeSummary> getClassFeeSummary(Long semesterId) {
        // 获取所有班级
        List<ClassInfo> classes = classInfoMapper.selectList(
                new LambdaQueryWrapper<ClassInfo>().eq(ClassInfo::getStatus, 1));

        List<ClassFeeSummary> summaries = new ArrayList<>();
        for (ClassInfo classInfo : classes) {
            // 获取班级学生
            List<Student> students = studentMapper.selectList(
                    new LambdaQueryWrapper<Student>()
                            .eq(Student::getClassId, classInfo.getId())
                            .eq(Student::getStatus, "active"));

            BigDecimal totalPaid = BigDecimal.ZERO;
            BigDecimal totalRefund = BigDecimal.ZERO;

            for (Student student : students) {
                // 缴费总额
                List<PaymentRecord> payments = paymentRecordMapper.selectList(
                        new LambdaQueryWrapper<PaymentRecord>()
                                .eq(PaymentRecord::getStudentId, student.getId())
                                .eq(PaymentRecord::getSemesterId, semesterId));
                BigDecimal paid = payments.stream()
                        .map(PaymentRecord::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                totalPaid = totalPaid.add(paid);

                // 退费总额（通过退费计算服务获取）
                try {
                    RefundCalculateResult refundResult = refundService.calculateRefund(
                            student.getId(), semesterId, null, null);
                    totalRefund = totalRefund.add(refundResult.getTotalRefundAmount());
                } catch (Exception e) {
                    // 退费计算失败时忽略，按0处理
                }
            }

            summaries.add(new ClassFeeSummary(
                    classInfo.getId(),
                    classInfo.getClassName(),
                    students.size(),
                    totalPaid,
                    totalRefund,
                    totalPaid.subtract(totalRefund)
            ));
        }

        return summaries;
    }

    @Override
    public void exportStudentFeeDetailPdf(Long studentId, Long semesterId, java.io.OutputStream outputStream) throws java.io.IOException {
        StudentFeeDetail detail = getStudentFeeDetail(studentId, semesterId);
        if (detail == null) {
            throw new IllegalArgumentException("学生不存在");
        }

        // 获取上学期信息
        Semester currentSemester = semesterMapper.selectById(semesterId);
        Long previousSemesterId = findPreviousSemesterId(currentSemester);
        Semester previousSemester = previousSemesterId != null ? semesterMapper.selectById(previousSemesterId) : null;
        
        // 获取上学期缴费记录
        List<PaymentRecord> previousPayments = new ArrayList<>();
        BigDecimal previousTotalPaid = BigDecimal.ZERO;
        if (previousSemesterId != null) {
            previousPayments = paymentRecordMapper.selectList(
                    new LambdaQueryWrapper<PaymentRecord>()
                            .eq(PaymentRecord::getStudentId, studentId)
                            .eq(PaymentRecord::getSemesterId, previousSemesterId));
            previousTotalPaid = previousPayments.stream()
                    .map(PaymentRecord::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            BaseFont baseFont = loadChineseFont();
            Font titleFont = new Font(baseFont, 16, Font.BOLD);
            Font sectionFont = new Font(baseFont, 12, Font.BOLD);
            Font textFont = new Font(baseFont, 10, Font.NORMAL);

            Paragraph title = new Paragraph("收费明细", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph info = new Paragraph(
                    String.format("学生：%s  班级：%s  学期：%s", detail.getStudentName(),
                            nullToEmpty(detail.getClassName()), nullToEmpty(detail.getSemesterName())),
                    textFont);
            info.setSpacingBefore(10);
            info.setSpacingAfter(10);
            document.add(info);

            // 上学期缴费记录（如果有）
            if (previousSemester != null && !previousPayments.isEmpty()) {
                Paragraph prevTitle = new Paragraph(
                        String.format("上学期（%s）缴费记录", previousSemester.getSemesterName()), sectionFont);
                prevTitle.setSpacingBefore(5);
                prevTitle.setSpacingAfter(5);
                document.add(prevTitle);

                PdfPTable prevTable = new PdfPTable(3);
                prevTable.setWidthPercentage(100);
                prevTable.setWidths(new float[]{2f, 1.5f, 1.5f});
                prevTable.addCell(createCell("费用类型", textFont, true));
                prevTable.addCell(createCell("缴费金额", textFont, true));
                prevTable.addCell(createCell("缴费日期", textFont, true));
                
                for (PaymentRecord payment : previousPayments) {
                    String feeTypeName = "";
                    if (payment.getFeeTypeId() != null) {
                        FeeType ft = feeTypeMapper.selectById(payment.getFeeTypeId());
                        feeTypeName = ft != null ? ft.getTypeName() : "";
                    }
                    prevTable.addCell(createCell(feeTypeName, textFont, false));
                    prevTable.addCell(createCell(formatAmount(payment.getAmount()), textFont, false));
                    prevTable.addCell(createCell(payment.getPayDate() != null ? payment.getPayDate().toString() : "", textFont, false));
                }
                document.add(prevTable);

                // 上学期汇总
                Paragraph prevSummary = new Paragraph(
                        String.format("上学期缴费总额：%s", formatAmount(previousTotalPaid)), textFont);
                prevSummary.setSpacingBefore(5);
                prevSummary.setSpacingAfter(10);
                document.add(prevSummary);
            }

            // 费用汇总（当前学期）
            Paragraph summaryTitle = new Paragraph("本学期费用汇总", sectionFont);
            summaryTitle.setSpacingBefore(5);
            summaryTitle.setSpacingAfter(5);
            document.add(summaryTitle);

            PdfPTable summaryTable = new PdfPTable(4);
            summaryTable.setWidthPercentage(100);
            summaryTable.setWidths(new float[]{1.5f, 1.5f, 1.5f, 1.5f});
            summaryTable.addCell(createCell("本学期应缴", textFont, true));
            summaryTable.addCell(createCell("上学期退费抵扣", textFont, true));
            summaryTable.addCell(createCell("实际应收", textFont, true));
            summaryTable.addCell(createCell("备注", textFont, true));
            summaryTable.addCell(createCell(formatAmount(detail.getTotalPaid()), textFont, false));
            summaryTable.addCell(createCell(formatAmount(detail.getTotalRefund()), textFont, false));
            summaryTable.addCell(createCell(formatAmount(detail.getActualAmount()), textFont, false));
            summaryTable.addCell(createCell("", textFont, false));
            document.add(summaryTable);

            Paragraph feeTitle = new Paragraph("费用类型明细", sectionFont);
            feeTitle.setSpacingBefore(10);
            feeTitle.setSpacingAfter(5);
            document.add(feeTitle);

            PdfPTable feeTable = new PdfPTable(4);
            feeTable.setWidthPercentage(100);
            feeTable.setWidths(new float[]{2f, 1.2f, 1.2f, 1.2f});
            feeTable.addCell(createCell("费用类型", textFont, true));
            feeTable.addCell(createCell("缴费金额", textFont, true));
            feeTable.addCell(createCell("退费金额", textFont, true));
            feeTable.addCell(createCell("实际金额", textFont, true));
            if (detail.getFeeTypeDetails() != null) {
                for (StudentFeeDetail.FeeTypeDetail item : detail.getFeeTypeDetails()) {
                    feeTable.addCell(createCell(nullToEmpty(item.getFeeTypeName()), textFont, false));
                    feeTable.addCell(createCell(formatAmount(item.getPaidAmount()), textFont, false));
                    feeTable.addCell(createCell(formatAmount(item.getRefundAmount()), textFont, false));
                    feeTable.addCell(createCell(formatAmount(item.getActualAmount()), textFont, false));
                }
            }
            document.add(feeTable);

            Paragraph refundTitle = new Paragraph("退费明细", sectionFont);
            refundTitle.setSpacingBefore(10);
            refundTitle.setSpacingAfter(5);
            document.add(refundTitle);

            if (detail.getRefundSegments() == null || detail.getRefundSegments().isEmpty()) {
                Paragraph emptyRefund = new Paragraph("无退费记录", textFont);
                emptyRefund.setSpacingAfter(5);
                document.add(emptyRefund);
            } else {
                for (StudentFeeDetail.RefundSegment segment : detail.getRefundSegments()) {
                    Paragraph segmentTitle = new Paragraph(
                            String.format("区间：%s ~ %s（%s天）  退费金额：%s",
                                    nullToEmpty(segment.getStartDate()),
                                    nullToEmpty(segment.getEndDate()),
                                    segment.getDays() == null ? "0" : segment.getDays(),
                                    formatAmount(segment.getSegmentAmount())),
                            textFont);
                    segmentTitle.setSpacingBefore(5);
                    segmentTitle.setSpacingAfter(3);
                    document.add(segmentTitle);

                    if (segment.getRefundItems() != null && !segment.getRefundItems().isEmpty()) {
                        PdfPTable refundTable = new PdfPTable(3);
                        refundTable.setWidthPercentage(90);
                        refundTable.setWidths(new float[]{2f, 1.2f, 1.2f});
                        refundTable.addCell(createCell("费用类型", textFont, true));
                        refundTable.addCell(createCell("日均退费", textFont, true));
                        refundTable.addCell(createCell("退费金额", textFont, true));
                        for (StudentFeeDetail.RefundItem item : segment.getRefundItems()) {
                            refundTable.addCell(createCell(nullToEmpty(item.getFeeTypeName()), textFont, false));
                            refundTable.addCell(createCell(formatAmount(item.getDailyRate()), textFont, false));
                            refundTable.addCell(createCell(formatAmount(item.getAmount()), textFont, false));
                        }
                        document.add(refundTable);
                    }
                }
            }

            Paragraph attendanceTitle = new Paragraph("考勤统计", sectionFont);
            attendanceTitle.setSpacingBefore(10);
            attendanceTitle.setSpacingAfter(5);
            document.add(attendanceTitle);

            StudentFeeDetail.AttendanceSummary attendance = detail.getAttendanceSummary();
            PdfPTable attendanceTable = new PdfPTable(5);
            attendanceTable.setWidthPercentage(100);
            attendanceTable.setWidths(new float[]{1.2f, 1.2f, 1.2f, 1.2f, 1.2f});
            attendanceTable.addCell(createCell("出勤天数", textFont, true));
            attendanceTable.addCell(createCell("请假天数", textFont, true));
            attendanceTable.addCell(createCell("缺勤天数", textFont, true));
            attendanceTable.addCell(createCell("总天数", textFont, true));
            attendanceTable.addCell(createCell("出勤率", textFont, true));
            attendanceTable.addCell(createCell(attendance == null ? "0" : String.valueOf(attendance.getPresentDays()), textFont, false));
            attendanceTable.addCell(createCell(attendance == null ? "0" : String.valueOf(attendance.getLeaveDays()), textFont, false));
            attendanceTable.addCell(createCell(attendance == null ? "0" : String.valueOf(attendance.getAbsentDays()), textFont, false));
            attendanceTable.addCell(createCell(attendance == null ? "0" : String.valueOf(attendance.getTotalDays()), textFont, false));
            attendanceTable.addCell(createCell(attendance == null ? "0%" : nullToEmpty(attendance.getAttendanceRate()), textFont, false));
            document.add(attendanceTable);

            Paragraph footer = new Paragraph(
                    "导出时间：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    textFont);
            footer.setSpacingBefore(10);
            document.add(footer);
        } catch (DocumentException e) {
            throw new java.io.IOException("生成PDF失败", e);
        } finally {
            document.close();
        }
    }

    /**
     * 构建学生收费明细
     */
    private StudentFeeDetail buildStudentFeeDetail(Student student, Long semesterId) {
        StudentFeeDetail detail = new StudentFeeDetail();
        detail.setStudentId(student.getId());
        detail.setStudentName(student.getName());
        detail.setClassId(student.getClassId());
        detail.setSemesterId(semesterId);

        // 获取班级名称
        ClassInfo classInfo = classInfoMapper.selectById(student.getClassId());
        if (classInfo != null) {
            detail.setClassName(classInfo.getClassName());
        }

        // 获取学期名称
        Semester semester = semesterMapper.selectById(semesterId);
        if (semester != null) {
            detail.setSemesterName(semester.getSemesterName());
        }

        // 获取上一个学期（用于计算退费）
        Long previousSemesterId = findPreviousSemesterId(semester);

        // 获取缴费记录
        List<PaymentRecord> payments = paymentRecordMapper.selectList(
                new LambdaQueryWrapper<PaymentRecord>()
                        .eq(PaymentRecord::getStudentId, student.getId())
                        .eq(PaymentRecord::getSemesterId, semesterId));

        // 获取所有费用类型
        List<FeeType> feeTypes = feeTypeMapper.selectList(
                new LambdaQueryWrapper<FeeType>().eq(FeeType::getStatus, 1));
        Map<Long, FeeType> feeTypeMap = feeTypes.stream()
                .collect(Collectors.toMap(FeeType::getId, ft -> ft));

        // 按费用类型汇总缴费
        Map<Long, BigDecimal> paidByType = payments.stream()
                .collect(Collectors.groupingBy(
                        PaymentRecord::getFeeTypeId,
                        Collectors.reducing(BigDecimal.ZERO, PaymentRecord::getAmount, BigDecimal::add)
                ));

        // 调用退费服务获取退费金额和请假明细（基于上学期）
        // 优先从数据库读取已保存的退费记录，如果没有则动态计算
        BigDecimal totalRefundAmount = BigDecimal.ZERO;
        List<StudentFeeDetail.RefundSegment> refundSegments = new ArrayList<>();
        int totalLeaveDays = 0;

        try {
            // 如果有上学期，则计算上学期的退费
            if (previousSemesterId != null) {
                // 先尝试获取已保存的退费记录
                RefundCalculateResult refundResult = refundService.getSavedRefund(student.getId(), previousSemesterId);
                
                // 如果没有保存的记录，则动态计算
                if (refundResult == null || refundResult.getTotalRefundAmount() == null 
                        || refundResult.getTotalRefundAmount().compareTo(BigDecimal.ZERO) == 0) {
                    refundResult = refundService.calculateRefund(student.getId(), previousSemesterId, null, null);
                }
                
                totalRefundAmount = refundResult.getTotalRefundAmount();
                totalLeaveDays = refundResult.getTotalLeaveDays() != null ? refundResult.getTotalLeaveDays() : 0;

                // 转换退费区间明细
                if (refundResult.getLeaveSegments() != null) {
                    for (LeaveSegment segment : refundResult.getLeaveSegments()) {
                        StudentFeeDetail.RefundSegment rs = new StudentFeeDetail.RefundSegment();
                        rs.setStartDate(segment.getStartDate());
                        rs.setEndDate(segment.getEndDate());
                        rs.setDays(segment.getDays());
                        rs.setMatchedRuleType(segment.getMatchedRuleType());
                        rs.setSegmentAmount(segment.getSegmentAmount());

                        // 转换退费明细项
                        if (segment.getRefundItems() != null) {
                            List<StudentFeeDetail.RefundItem> items = new ArrayList<>();
                            for (RefundItem item : segment.getRefundItems()) {
                                StudentFeeDetail.RefundItem ri = new StudentFeeDetail.RefundItem();
                                ri.setFeeTypeCode(item.getFeeTypeCode());
                                ri.setFeeTypeName(item.getFeeTypeName());
                                ri.setDailyRate(item.getDailyRate());
                                ri.setAmount(item.getAmount());
                                items.add(ri);
                            }
                            rs.setRefundItems(items);
                        }
                        refundSegments.add(rs);
                    }
                }
            }
            
            // 如果学生已离园，还需要检查当前学期的离园退费
            if ("inactive".equals(student.getStatus()) && student.getLeaveDate() != null) {
                RefundCalculateResult dropoutResult = refundService.getSavedDropoutRefund(student.getId(), semesterId);
                
                if (dropoutResult == null) {
                    dropoutResult = refundService.calculateDropoutRefund(student.getId(), semesterId);
                }
                
                if (dropoutResult != null && dropoutResult.getTotalRefundAmount() != null
                        && dropoutResult.getTotalRefundAmount().compareTo(BigDecimal.ZERO) > 0) {
                    // 累加离园退费金额
                    totalRefundAmount = totalRefundAmount.add(dropoutResult.getTotalRefundAmount());
                    
                    // 添加离园退费明细
                    if (dropoutResult.getLeaveSegments() != null) {
                        for (LeaveSegment segment : dropoutResult.getLeaveSegments()) {
                            StudentFeeDetail.RefundSegment rs = new StudentFeeDetail.RefundSegment();
                            rs.setStartDate(segment.getStartDate());
                            rs.setEndDate(segment.getEndDate());
                            rs.setDays(segment.getDays());
                            rs.setMatchedRuleType("DROPOUT"); // 标记为离园退费
                            rs.setSegmentAmount(segment.getSegmentAmount());

                            if (segment.getRefundItems() != null) {
                                List<StudentFeeDetail.RefundItem> items = new ArrayList<>();
                                for (RefundItem item : segment.getRefundItems()) {
                                    StudentFeeDetail.RefundItem ri = new StudentFeeDetail.RefundItem();
                                    ri.setFeeTypeCode(item.getFeeTypeCode());
                                    ri.setFeeTypeName(item.getFeeTypeName());
                                    ri.setDailyRate(item.getDailyRate());
                                    ri.setAmount(item.getAmount());
                                    items.add(ri);
                                }
                                rs.setRefundItems(items);
                            }
                            refundSegments.add(rs);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 退费计算失败时忽略，按0处理
        }

        // 按费用类型汇总退费金额
        Map<String, BigDecimal> refundByTypeCode = new java.util.HashMap<>();
        for (StudentFeeDetail.RefundSegment segment : refundSegments) {
            if (segment.getRefundItems() != null) {
                for (StudentFeeDetail.RefundItem item : segment.getRefundItems()) {
                    refundByTypeCode.merge(item.getFeeTypeCode(), item.getAmount(), BigDecimal::add);
                }
            }
        }

        // 构建费用类型明细
        List<StudentFeeDetail.FeeTypeDetail> feeTypeDetails = new ArrayList<>();
        BigDecimal totalPaid = BigDecimal.ZERO;

        for (FeeType feeType : feeTypes) {
            StudentFeeDetail.FeeTypeDetail ftDetail = new StudentFeeDetail.FeeTypeDetail();
            ftDetail.setFeeTypeId(feeType.getId());
            ftDetail.setTypeCode(feeType.getTypeCode());
            ftDetail.setFeeTypeName(feeType.getTypeName());

            BigDecimal paid = paidByType.getOrDefault(feeType.getId(), BigDecimal.ZERO);
            BigDecimal refund = refundByTypeCode.getOrDefault(feeType.getTypeCode(), BigDecimal.ZERO);

            ftDetail.setPaidAmount(paid);
            ftDetail.setRefundAmount(refund);
            ftDetail.setActualAmount(paid.subtract(refund));

            feeTypeDetails.add(ftDetail);

            totalPaid = totalPaid.add(paid);
        }

        detail.setFeeTypeDetails(feeTypeDetails);
        detail.setTotalPaid(totalPaid);
        detail.setTotalRefund(totalRefundAmount);
        detail.setActualAmount(totalPaid.subtract(totalRefundAmount));
        detail.setRefundSegments(refundSegments);

        // 获取上学期数据
        if (previousSemesterId != null) {
            Semester prevSemester = semesterMapper.selectById(previousSemesterId);
            detail.setPreviousSemesterName(prevSemester != null ? prevSemester.getSemesterName() : null);
            
            RefundCalculateResult prevRefund = refundService.getSavedRefund(student.getId(), previousSemesterId);
            if (prevRefund != null) {
                detail.setPreviousSemesterRefund(prevRefund.getTotalRefundAmount());
                if (prevRefund.getLeaveSegments() != null) {
                    List<StudentFeeDetail.RefundSegment> prevSegments = new ArrayList<>();
                    for (LeaveSegment seg : prevRefund.getLeaveSegments()) {
                        StudentFeeDetail.RefundSegment rs = new StudentFeeDetail.RefundSegment();
                        rs.setStartDate(seg.getStartDate());
                        rs.setEndDate(seg.getEndDate());
                        rs.setDays(seg.getDays());
                        rs.setMatchedRuleType(seg.getMatchedRuleType());
                        rs.setSegmentAmount(seg.getSegmentAmount());
                        if (seg.getRefundItems() != null) {
                            List<StudentFeeDetail.RefundItem> items = new ArrayList<>();
                            for (RefundItem item : seg.getRefundItems()) {
                                StudentFeeDetail.RefundItem ri = new StudentFeeDetail.RefundItem();
                                ri.setFeeTypeCode(item.getFeeTypeCode());
                                ri.setFeeTypeName(item.getFeeTypeName());
                                ri.setDailyRate(item.getDailyRate());
                                ri.setAmount(item.getAmount());
                                items.add(ri);
                            }
                            rs.setRefundItems(items);
                        }
                        prevSegments.add(rs);
                    }
                    detail.setPreviousSemesterRefundSegments(prevSegments);
                }
            }
            
            List<PaymentRecord> prevPayments = paymentRecordMapper.selectList(
                    new LambdaQueryWrapper<PaymentRecord>()
                            .eq(PaymentRecord::getStudentId, student.getId())
                            .eq(PaymentRecord::getSemesterId, previousSemesterId));
            BigDecimal prevPaid = prevPayments.stream()
                    .map(PaymentRecord::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            detail.setPreviousSemesterPaid(prevPaid);
            detail.setPreviousSemesterActual(prevPaid.subtract(detail.getPreviousSemesterRefund() != null ? detail.getPreviousSemesterRefund() : BigDecimal.ZERO));
            
            // 转换上学期缴费记录
            List<StudentFeeDetail.PaymentItem> paymentItems = new ArrayList<>();
            for (PaymentRecord payment : prevPayments) {
                StudentFeeDetail.PaymentItem item = new StudentFeeDetail.PaymentItem();
                FeeType feeType = payment.getFeeTypeId() != null ? feeTypeMap.get(payment.getFeeTypeId()) : null;
                item.setFeeTypeName(feeType != null ? feeType.getTypeName() : "");
                item.setAmount(payment.getAmount());
                item.setPayDate(payment.getPayDate() != null ? payment.getPayDate().toString() : "");
                paymentItems.add(item);
            }
            detail.setPreviousSemesterPayments(paymentItems);
        }

        // 获取考勤统计（请假天数从退费计算结果获取）
        detail.setAttendanceSummary(buildAttendanceSummary(student.getId(), semester, totalLeaveDays));

        return detail;
    }

    /**
     * 构建考勤统计
     *
     * @param studentId 学生ID
     * @param semester 学期
     * @param leaveDaysFromRefund 从退费计算获取的请假天数（用于显示，实际统计从考勤表获取）
     */
    private StudentFeeDetail.AttendanceSummary buildAttendanceSummary(Long studentId, Semester semester, int leaveDaysFromRefund) {
        StudentFeeDetail.AttendanceSummary summary = new StudentFeeDetail.AttendanceSummary();

        if (semester == null) {
            summary.setPresentDays(0);
            summary.setLeaveDays(0);
            summary.setAbsentDays(0);
            summary.setTotalDays(0);
            summary.setAttendanceRate("0%");
            return summary;
        }

        // 查询学期内的考勤记录
        List<Attendance> attendances = attendanceMapper.selectList(
                new LambdaQueryWrapper<Attendance>()
                        .eq(Attendance::getStudentId, studentId)
                        .ge(Attendance::getAttendDate, semester.getStartDate())
                        .le(Attendance::getAttendDate, semester.getEndDate()));

        int presentDays = 0;
        int leaveDays = 0;
        int absentDays = 0;

        for (Attendance att : attendances) {
            switch (att.getStatus()) {
                case "present" -> presentDays++;
                case "leave" -> leaveDays++;
                case "absent" -> absentDays++;
            }
        }

        int totalDays = presentDays + leaveDays + absentDays;
        String rate = totalDays > 0
                ? String.format("%.1f%%", (double) presentDays / totalDays * 100)
                : "0%";

        summary.setPresentDays(presentDays);
        summary.setLeaveDays(leaveDays);
        summary.setAbsentDays(absentDays);
        summary.setTotalDays(totalDays);
        summary.setAttendanceRate(rate);

        return summary;
    }

    private PdfPCell createCell(String content, Font font, boolean header) {
        PdfPCell cell = new PdfPCell(new Phrase(content == null ? "" : content, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6f);
        if (header) {
            cell.setBackgroundColor(new java.awt.Color(240, 240, 240));
        }
        return cell;
    }

    private BaseFont loadChineseFont() throws java.io.IOException, DocumentException {
        try (java.io.InputStream fontStream = getClass().getClassLoader()
                .getResourceAsStream("fonts/NotoSansSC-Regular.otf")) {
            if (fontStream == null) {
                // 使用系统默认中文字体
                return BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            }
            Path tempFont = Files.createTempFile("NotoSansSC-Regular", ".otf");
            Files.copy(fontStream, tempFont, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            tempFont.toFile().deleteOnExit();
            // 使用 IDENTITY_H 编码并嵌入字体，确保跨平台兼容
            return BaseFont.createFont(tempFont.toAbsolutePath().toString(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (Exception ex) {
            // 回退到系统默认中文字体
            return BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        }
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    /**
     * 查找上一个学期ID
     * <p>
     * 根据当前学期的开始日期，查找结束日期最接近但不超过当前学期开始日期的学期
     * </p>
     *
     * @param currentSemester 当前学期
     * @return 上一个学期ID，如果没有则返回null
     */
    private Long findPreviousSemesterId(Semester currentSemester) {
        if (currentSemester == null || currentSemester.getStartDate() == null) {
            return null;
        }

        // 查询所有启用的学期
        List<Semester> allSemesters = semesterMapper.selectList(
                new LambdaQueryWrapper<Semester>()
                        .eq(Semester::getStatus, 1)
                        .orderByDesc(Semester::getEndDate));

        // 找到当前学期的索引
        for (int i = 0; i < allSemesters.size(); i++) {
            if (allSemesters.get(i).getId().equals(currentSemester.getId())) {
                // 返回下一个（时间更早的）学期
                if (i + 1 < allSemesters.size()) {
                    return allSemesters.get(i + 1).getId();
                }
                break;
            }
        }

        return null;
    }

}
