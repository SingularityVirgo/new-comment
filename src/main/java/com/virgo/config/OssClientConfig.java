package com.virgo.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.virgo.config.properties.OssProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@EnableConfigurationProperties(OssProperties.class)
public class OssClientConfig {

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnProperty(name = "aliyun.oss.enabled", havingValue = "true")
    public OSS ossClient(OssProperties p) {
        if (!StringUtils.hasText(p.getAccessKeyId()) || !StringUtils.hasText(p.getAccessKeySecret())) {
            throw new IllegalStateException("aliyun.oss.enabled=true 但未配置 aliyun.oss.access-key-id / access-key-secret（建议使用环境变量注入）");
        }
        if (!StringUtils.hasText(p.getBucketName())) {
            throw new IllegalStateException("aliyun.oss.enabled=true 但未配置 aliyun.oss.bucket-name");
        }
        return new OSSClientBuilder().build(p.getEndpoint(), p.getAccessKeyId(), p.getAccessKeySecret());
    }
}
