package com.kindergarten.system.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * OpenAPI (Swagger) 配置类
 * <p>
 * 仅在 dev 环境启用 API 文档，生产环境不暴露。
 * 访问地址：http://127.0.0.1:8080/api/swagger-ui.html
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
@Configuration
@Profile({"dev", "local"})
public class OpenApiConfig {

    /**
     * 配置 OpenAPI 文档信息
     *
     * @return OpenAPI 配置
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("幼儿园管理系统 API")
                        .description("幼儿园管理系统后端接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Kindergarten System")
                                .email("support@kindergarten.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }

}
