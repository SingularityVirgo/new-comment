package com.virgo.service.storage;

import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.virgo.common.exception.BizException;
import com.virgo.config.properties.OssProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

@RequiredArgsConstructor
public class OssBlogImageStorage implements BlogImageStorage {

    private final OSS ossClient;
    private final OssProperties ossProperties;

    @Override
    public String store(MultipartFile image) throws IOException {
        String originalFilename = image.getOriginalFilename();
        String objectKey = buildObjectKey(originalFilename);
        try (InputStream in = image.getInputStream()) {
            ossClient.putObject(ossProperties.getBucketName(), objectKey, in);
        } catch (OSSException e) {
            throw new IOException("OSS 上传失败: " + e.getMessage(), e);
        }
        return publicUrl(objectKey);
    }

    @Override
    public void delete(String storedRef) {
        String key = toObjectKey(storedRef);
        if (!StringUtils.hasText(key) || key.contains("..")) {
            throw new BizException("错误的文件名称");
        }
        ossClient.deleteObject(ossProperties.getBucketName(), key);
    }

    private String buildObjectKey(String originalFilename) {
        String suffix = StrUtil.subAfter(originalFilename, ".", true);
        String name = UUID.randomUUID().toString();
        int hash = name.hashCode();
        int d1 = hash & 0xF;
        int d2 = (hash >> 4) & 0xF;
        return StrUtil.format("blogs/{}/{}/{}.{}", d1, d2, name, suffix);
    }

    private String publicUrl(String objectKey) {
        String base = ossProperties.getPublicBaseUrl().replaceAll("/+$", "");
        return base + "/" + objectKey;
    }

    /**
     * 支持：完整 HTTPS URL、以 / 开头的相对路径（与本地存储一致）、纯 object key。
     */
    static String toObjectKey(String storedRef) {
        if (!StringUtils.hasText(storedRef)) {
            return "";
        }
        String s = storedRef.trim();
        if (s.startsWith("http://") || s.startsWith("https://")) {
            try {
                URI u = URI.create(s);
                String path = u.getPath();
                if (path.startsWith("/")) {
                    return path.substring(1);
                }
                return path;
            } catch (IllegalArgumentException e) {
                return "";
            }
        }
        if (s.startsWith("/")) {
            return s.substring(1);
        }
        return s;
    }
}
