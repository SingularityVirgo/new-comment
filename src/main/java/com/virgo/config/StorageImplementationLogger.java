package com.virgo.config;

import com.virgo.service.storage.BlogImageStorage;
import com.virgo.service.storage.UserAvatarStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 启动后打印实际生效的存储实现（仅 OSS）。
 */
@Slf4j
@Configuration
public class StorageImplementationLogger {

    @Bean
    ApplicationRunner logStorageImplementations(UserAvatarStorage avatar, BlogImageStorage blog) {
        return (ApplicationArguments args) -> {
            log.info("[storage] UserAvatarStorage -> {}", avatar.getClass().getSimpleName());
            log.info("[storage] BlogImageStorage -> {}", blog.getClass().getSimpleName());
        };
    }
}
