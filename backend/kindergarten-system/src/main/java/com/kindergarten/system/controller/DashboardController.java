package com.kindergarten.system.controller;

import com.kindergarten.system.common.result.Result;
import com.kindergarten.system.dto.DashboardOverview;
import com.kindergarten.system.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 获取首页概览数据
     *
     * @return 首页统计数据
     */
    @GetMapping("/overview")
    public Result<DashboardOverview> overview() {
        return Result.success(dashboardService.getOverview());
    }

}
