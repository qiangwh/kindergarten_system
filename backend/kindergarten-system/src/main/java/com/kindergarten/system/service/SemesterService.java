package com.kindergarten.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kindergarten.system.entity.Semester;

public interface SemesterService extends IService<Semester> {

    void setCurrent(Long id);

    Semester findCurrent();

    /**
     * 分页查询学期列表
     *
     * @param semesterName 学期名称（模糊查询）
     * @param page         页码
     * @param pageSize     每页条数
     * @return 分页数据
     */
    IPage<Semester> pageList(String semesterName, Long page, Long pageSize);
}