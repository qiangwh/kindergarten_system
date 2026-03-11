package com.kindergarten.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kindergarten.system.entity.Semester;

public interface SemesterService extends IService<Semester> {

    void setCurrent(Long id);

    Semester findCurrent();
}