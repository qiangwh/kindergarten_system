package com.kindergarten.system.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "r2")
public class R2Properties {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String publicUrl;
    private String region = "auto";
    private Long maxFileSizeMb = 10L;
}