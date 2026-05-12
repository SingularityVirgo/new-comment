package com.virgo.service.storage;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.virgo.common.exception.BizException;
import com.virgo.utils.SystemConstants;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class LocalBlogImageStorage implements BlogImageStorage {

    @Override
    public String store(MultipartFile image) throws IOException {
        String originalFilename = image.getOriginalFilename();
        String fileName = createNewFileName(originalFilename);
        image.transferTo(new File(SystemConstants.IMAGE_UPLOAD_DIR, fileName));
        return fileName;
    }

    @Override
    public void delete(String storedRef) {
        File file = new File(SystemConstants.IMAGE_UPLOAD_DIR, storedRef);
        if (file.isDirectory()) {
            throw new BizException("错误的文件名称");
        }
        FileUtil.del(file);
    }

    private String createNewFileName(String originalFilename) {
        String suffix = StrUtil.subAfter(originalFilename, ".", true);
        String name = UUID.randomUUID().toString();
        int hash = name.hashCode();
        int d1 = hash & 0xF;
        int d2 = (hash >> 4) & 0xF;
        File dir = new File(SystemConstants.IMAGE_UPLOAD_DIR, StrUtil.format("/blogs/{}/{}", d1, d2));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return StrUtil.format("/blogs/{}/{}/{}.{}", d1, d2, name, suffix);
    }
}
