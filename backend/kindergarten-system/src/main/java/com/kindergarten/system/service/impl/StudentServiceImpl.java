package com.kindergarten.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kindergarten.system.common.exception.BusinessException;
import com.kindergarten.system.common.result.ResultCode;
import com.kindergarten.system.dto.StudentPageQuery;
import com.kindergarten.system.entity.Student;
import com.kindergarten.system.mapper.StudentMapper;
import com.kindergarten.system.service.StudentService;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

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
}