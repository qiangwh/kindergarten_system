package com.kindergarten.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kindergarten.system.dto.AttendanceListQuery;
import com.kindergarten.system.dto.AttendanceMonthStatItem;
import com.kindergarten.system.dto.AttendanceSaveRequest;
import com.kindergarten.system.dto.AttendanceStudentStatItem;
import com.kindergarten.system.entity.Attendance;

import java.util.List;

public interface AttendanceService extends IService<Attendance> {

    List<Attendance> listByDate(AttendanceListQuery query);

    void saveBatchAttendance(AttendanceSaveRequest request);

    List<AttendanceStudentStatItem> statByStudent(Long classId, String startDate, String endDate);

    AttendanceMonthStatItem statByMonth(Long classId);
}