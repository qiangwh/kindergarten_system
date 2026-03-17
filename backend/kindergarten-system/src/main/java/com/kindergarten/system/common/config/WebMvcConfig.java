package com.kindergarten.system.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 * <p>
 * CORS 配置已移至 SecurityConfig
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    // CORS 配置在 SecurityConfig 中统一处理
}
