package com.kindergarten.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kindergarten.system.entity.ClassInfo;

import java.util.List;

public interface ClassInfoService extends IService<ClassInfo> {

    List<ClassInfo> listEnabled();
}