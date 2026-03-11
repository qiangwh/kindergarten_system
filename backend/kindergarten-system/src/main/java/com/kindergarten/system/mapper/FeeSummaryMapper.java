package com.kindergarten.system.mapper;

import com.kindergarten.system.dto.ClassFeeSummaryItem;
import com.kindergarten.system.dto.ClassFeeTypeSummaryItem;
import com.kindergarten.system.dto.FeeTypeSummaryItem;
import com.kindergarten.system.dto.SemesterFeeCompareItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface FeeSummaryMapper {

    BigDecimal selectSemesterTotal(@Param("semesterId") Long semesterId);

    List<FeeTypeSummaryItem> selectSemesterByFeeType(@Param("semesterId") Long semesterId);

    List<SemesterFeeCompareItem> selectSemesterCompare(@Param("semesterIds") List<Long> semesterIds);

    List<ClassFeeSummaryItem> selectByClass(@Param("semesterId") Long semesterId);

    List<ClassFeeTypeSummaryItem> selectByClassAndFeeType(@Param("semesterId") Long semesterId);
}