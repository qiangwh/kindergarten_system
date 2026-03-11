package com.kindergarten.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kindergarten.system.dto.AttendanceListQuery;
import com.kindergarten.system.dto.AttendanceMonthStatItem;
import com.kindergarten.system.dto.AttendanceStudentStatItem;
import com.kindergarten.system.entity.Attendance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AttendanceMapper extends BaseMapper<Attendance> {

    List<Attendance> selectAttendanceList(@Param("query") AttendanceListQuery query);

    List<AttendanceStudentStatItem> selectStudentStats(@Param("classId") Long classId,
                                                       @Param("startDate") String startDate,
                                                       @Param("endDate") String endDate);

    AttendanceMonthStatItem selectMonthSummary(@Param("classId") Long classId,
                                               @Param("startDate") String startDate,
                                               @Param("endDate") String endDate);

    List<String> selectLeaveDates(@Param("studentId") Long studentId,
                                  @Param("startDate") String startDate,
                                  @Param("endDate") String endDate);
}