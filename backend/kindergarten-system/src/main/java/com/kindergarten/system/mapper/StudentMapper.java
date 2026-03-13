package com.kindergarten.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kindergarten.system.dto.StudentExportQuery;
import com.kindergarten.system.dto.StudentPageQuery;
import com.kindergarten.system.entity.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {

    IPage<Student> selectStudentPage(IPage<Student> page, @Param("query") StudentPageQuery query);

    java.util.List<Student> selectStudentsForExport(@Param("query") StudentExportQuery query);

    /**
     * 查询指定学号前缀的最大序号
     * @param prefix 学号前缀（如：2024-XB1-）
     * @return 最大序号（如：001返回1，002返回2）
     */
    Integer selectMaxStudentNoSequence(@Param("prefix") String prefix);
}