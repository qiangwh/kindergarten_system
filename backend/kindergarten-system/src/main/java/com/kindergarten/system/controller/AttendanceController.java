package com.kindergarten.system.controller;

import com.kindergarten.system.common.result.Result;
import com.kindergarten.system.dto.AttendanceListQuery;
import com.kindergarten.system.dto.AttendanceMonthStatItem;
import com.kindergarten.system.dto.AttendanceSaveRequest;
import com.kindergarten.system.dto.AttendanceStudentStatItem;
import com.kindergarten.system.entity.Attendance;
import com.kindergarten.system.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/list")
    public Result<List<Attendance>> list(AttendanceListQuery query) {
        return Result.success(attendanceService.listByDate(query));
    }

    @PostMapping("/save")
    public Result<Void> save(@RequestBody @Valid AttendanceSaveRequest request) {
        attendanceService.saveBatchAttendance(request);
        return Result.success();
    }

    @GetMapping("/statByStudent")
    public Result<List<AttendanceStudentStatItem>> statByStudent(@RequestParam Long classId,
                                                                 @RequestParam String startDate,
                                                                 @RequestParam String endDate) {
        return Result.success(attendanceService.statByStudent(classId, startDate, endDate));
    }

    @GetMapping("/statByMonth")
    public Result<AttendanceMonthStatItem> statByMonth(@RequestParam Long classId) {
        return Result.success(attendanceService.statByMonth(classId));
    }
}