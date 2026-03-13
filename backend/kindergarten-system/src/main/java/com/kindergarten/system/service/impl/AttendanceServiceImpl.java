package com.kindergarten.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kindergarten.system.dto.AttendanceListQuery;
import com.kindergarten.system.dto.AttendanceMonthStatItem;
import com.kindergarten.system.dto.AttendanceSaveItem;
import com.kindergarten.system.dto.AttendanceSaveRequest;
import com.kindergarten.system.dto.AttendanceStudentStatItem;
import com.kindergarten.system.entity.Attendance;
import com.kindergarten.system.mapper.AttendanceMapper;
import com.kindergarten.system.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl extends ServiceImpl<AttendanceMapper, Attendance> implements AttendanceService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

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

        QueryWrapper<Attendance> wrapper = new QueryWrapper<>();
        wrapper.eq("attend_date", attendDate)
               .in("student_id", studentIds);
        remove(wrapper);

        Map<Long, AttendanceSaveItem> itemMap = items.stream()
                .collect(Collectors.toMap(
                        AttendanceSaveItem::getStudentId,
                        Function.identity(),
                        (left, right) -> right
                ));

        for (AttendanceSaveItem item : itemMap.values()) {
            Attendance attendance = new Attendance();
            attendance.setClassId(request.getClassId());
            attendance.setStudentId(item.getStudentId());
            attendance.setAttendDate(attendDate);
            attendance.setStatus(item.getStatus());
            attendance.setRemark(item.getRemark());
            save(attendance);
        }
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