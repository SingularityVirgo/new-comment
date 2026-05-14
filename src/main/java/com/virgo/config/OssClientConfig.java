package com.virgo.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.virgo.config.properties.OssProperties;
import com.virgo.service.storage.BlogImageStorage;
import com.virgo.service.storage.OssBlogImageStorage;
import com.virgo.service.storage.OssUserAvatarStorage;
import com.virgo.service.storage.UserAvatarStorage;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * 阿里云 OSS 客户端与图片存储实现（唯一存储方式）。
 */
@Configuration
@EnableConfigurationProperties(OssProperties.class)
public class OssClientConfig {

    @Bean(destroyMethod = "shutdown")
    public OSS ossClient(OssProperties p, Environment env) {
        if (!StringUtils.hasText(p.getEndpoint())) {
            throw new IllegalStateException("aliyun.oss.endpoint is blank.");
        }
        String accessKeyId =
                firstNonBlank(
                        p.getAccessKeyId(),
                        env.getProperty("ALIYUN_OSS_ACCESS_KEY_ID"),
                        env.getProperty("ALIBABA_CLOUD_ACCESS_KEY_ID"),
                        env.getProperty("OSS_ACCESS_KEY_ID"));
        String accessKeySecret =
                firstNonBlank(
                        p.getAccessKeySecret(),
                        env.getProperty("ALIYUN_OSS_ACCESS_KEY_SECRET"),
                        env.getProperty("ALIBABA_CLOUD_ACCESS_KEY_SECRET"),
                        env.getProperty("OSS_ACCESS_KEY_SECRET"));
        if (!StringUtils.hasText(accessKeyId) || !StringUtils.hasText(accessKeySecret)) {
            throw new IllegalStateException(
                    "OSS credentials are blank. Set aliyun.oss.access-key-id/secret or "
                            + "ALIYUN_OSS_ACCESS_KEY_ID / ALIYUN_OSS_ACCESS_KEY_SECRET (or ALIBABA_CLOUD_* / OSS_*).");
        }
        if (!StringUtils.hasText(p.getBucketName())) {
            throw new IllegalStateException("aliyun.oss.bucket-name is blank.");
        }
        return new OSSClientBuilder().build(p.getEndpoint(), accessKeyId.trim(), accessKeySecret.trim());
    }

    private static String firstNonBlank(String primary, String... fallbacks) {
        if (StringUtils.hasText(primary)) {
            return primary.trim();
        }
        if (fallbacks != null) {
            for (String s : fallbacks) {
                if (StringUtils.hasText(s)) {
                    return s.trim();
                }
            }
        }
        return "";
    }

    @Bean
    @DependsOn("ossClient")
    public UserAvatarStorage userAvatarStorage(OSS ossClient, OssProperties ossProperties) {
        if (!StringUtils.hasText(ossProperties.getPublicBaseUrl())) {
            throw new IllegalStateException("aliyun.oss.public-base-url is blank.");
        }
        return new OssUserAvatarStorage(ossClient, ossProperties);
    }

    @Bean
    @DependsOn("ossClient")
    public BlogImageStorage blogImageStorage(OSS ossClient, OssProperties ossProperties) {
        if (!StringUtils.hasText(ossProperties.getPublicBaseUrl())) {
            throw new IllegalStateException("aliyun.oss.public-base-url is blank.");
        }
        return new OssBlogImageStorage(ossClient, ossProperties);
    }
}
