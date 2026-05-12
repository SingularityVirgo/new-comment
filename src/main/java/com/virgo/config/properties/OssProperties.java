package com.virgo.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssProperties {

    /**
     * 为 true 时使用 OSS；为 false 时沿用本地目录（{@link com.virgo.utils.SystemConstants#IMAGE_UPLOAD_DIR}）。
     */
    private boolean enabled = false;

    /** 例如 https://oss-cn-beijing.aliyuncs.com */
    private String endpoint = "https://oss-cn-beijing.aliyuncs.com";

    private String bucketName = "";

    private String accessKeyId = "";

    private String accessKeySecret = "";

    /**
     * 浏览器访问对象使用的根 URL，无尾部斜杠，例如 https://bucket.oss-cn-beijing.aliyuncs.com
     * 若绑定了 CDN 或自定义域名，填该域名即可。
     */
    private String publicBaseUrl = "";
}
