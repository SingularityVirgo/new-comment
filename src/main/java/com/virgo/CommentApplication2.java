package com.virgo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.virgo.mapper")
@SpringBootApplication
public class CommentApplication2 {

    public static void main(String[] args) {
        SpringApplication.run(CommentApplication2.class, args);
    }

}
