package com.virgo.config;

import com.aliyun.oss.OSS;
import com.virgo.config.properties.OssProperties;
import com.virgo.service.storage.BlogImageStorage;
import com.virgo.service.storage.LocalBlogImageStorage;
import com.virgo.service.storage.OssBlogImageStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class BlogImageStorageConfig {

    @Bean
    @ConditionalOnBean(OSS.class)
    public BlogImageStorage ossBlogImageStorage(OSS ossClient, OssProperties ossProperties) {
        if (!StringUtils.hasText(ossProperties.getPublicBaseUrl())) {
            throw new IllegalStateException("aliyun.oss.enabled=true 时请配置 aliyun.oss.public-base-url（对象对外访问根 URL，无尾部斜杠）");
        }
        return new OssBlogImageStorage(ossClient, ossProperties);
    }

    @Bean
    @ConditionalOnMissingBean(BlogImageStorage.class)
    public BlogImageStorage localBlogImageStorage() {
        return new LocalBlogImageStorage();
    }
}
