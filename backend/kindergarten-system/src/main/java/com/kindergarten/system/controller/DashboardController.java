package com.kindergarten.system.controller;

import com.kindergarten.system.common.result.Result;
import com.kindergarten.system.dto.DashboardOverview;
import com.kindergarten.system.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页仪表盘控制器
 * <p>
 * 提供首页概览统计数据。
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    /** 仪表盘服务 */
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
