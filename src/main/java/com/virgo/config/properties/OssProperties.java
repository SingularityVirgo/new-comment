package com.virgo.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssProperties {

    /** 例如 https://oss-cn-beijing.aliyuncs.com */
    private String endpoint = "https://oss-cn-beijing.aliyuncs.com";

    private String bucketName = "";

    private String accessKeyId = "";

    private String accessKeySecret = "";

    /**
     * 浏览器访问对象使用的根 URL，无尾部斜杠，例如 https://bucket.oss-cn-beijing.aliyuncs.com
     */
    private String publicBaseUrl = "";

    /**
     * 上传时写入 public-read，便于浏览器直接访问 URL。Bucket 为「禁止 ACL」或仅用策略授权时设为 false，并在控制台配置公共读策略。
     */
    private boolean uploadPublicRead = true;
}
