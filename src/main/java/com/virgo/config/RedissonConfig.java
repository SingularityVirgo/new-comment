package com.virgo.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient(
            @Value("${spring.data.redis.host}") String host,
            @Value("${spring.data.redis.port}") int port,
            @Value("${spring.data.redis.password:}") String password) {
        Config config = new Config();
        var server = config.useSingleServer().setAddress("redis://" + host + ":" + port);
        if (password != null && !password.isBlank()) {
            server.setPassword(password);
        }
        // 与 Lettuce 池同量级，避免异步下单抢锁时 Redisson 连接成为短板
        server.setConnectionPoolSize(128);
        server.setConnectionMinimumIdleSize(32);
        return Redisson.create(config);
    }
}
