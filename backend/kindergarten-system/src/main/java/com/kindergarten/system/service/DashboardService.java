package com.kindergarten.system.service;

import com.kindergarten.system.dto.DashboardOverview;

public interface DashboardService {

    /**
     * 获取首页概览数据
     *
     * @return 首页统计数据
     */
    DashboardOverview getOverview();

}
