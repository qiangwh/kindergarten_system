package com.kindergarten.system.controller;

import com.kindergarten.system.common.result.Result;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查 / 保活接口
 * <p>
 * 用于 Render 等平台的健康检查和外部定时 ping 保活。
 * </p>
 */
@RestController
@RequestMapping
public class HealthController {

    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "UP");
        data.put("service", "kindergarten-system");
        data.put("message", "service is running");
        data.put("timestamp", System.currentTimeMillis());
        return Result.success(data);
    }
}