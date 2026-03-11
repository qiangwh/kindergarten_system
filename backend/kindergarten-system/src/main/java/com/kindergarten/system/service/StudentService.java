package com.kindergarten.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kindergarten.system.dto.StudentPageQuery;
import com.kindergarten.system.entity.Student;

public interface StudentService extends IService<Student> {

    IPage<Student> pageStudents(StudentPageQuery query);

    void updateStatus(Long id, String status);
}