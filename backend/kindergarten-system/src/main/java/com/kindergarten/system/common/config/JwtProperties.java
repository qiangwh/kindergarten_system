package com.kindergarten.system.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置项
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** 签名密钥 */
    private String secret;

    /** 过期时间，单位：毫秒 */
    private Long expiration;

    /** 签发者 */
    private String issuer = "kindergarten-system";

}