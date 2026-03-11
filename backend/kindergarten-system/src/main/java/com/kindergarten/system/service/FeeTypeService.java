package com.kindergarten.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kindergarten.system.entity.FeeType;

import java.util.List;

public interface FeeTypeService extends IService<FeeType> {

    List<FeeType> listEnabled();
}