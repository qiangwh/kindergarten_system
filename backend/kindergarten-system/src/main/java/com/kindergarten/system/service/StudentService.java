package com.kindergarten.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kindergarten.system.dto.StudentExportQuery;
import com.kindergarten.system.dto.StudentImportResult;
import com.kindergarten.system.dto.StudentPageQuery;
import com.kindergarten.system.entity.Student;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface StudentService extends IService<Student> {

    IPage<Student> pageStudents(StudentPageQuery query);

    void updateStatus(Long id, String status);

    List<Student> listStudentsForExport(StudentExportQuery query);

    void exportStudents(StudentExportQuery query, OutputStream outputStream) throws IOException;

    StudentImportResult importStudents(MultipartFile file) throws IOException;
}