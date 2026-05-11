package com.virgo.controller;

import com.virgo.common.exception.BizException;
import com.virgo.service.storage.BlogImageStorage;
import com.virgo.web.api.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("upload")
@RequiredArgsConstructor
public class UploadController {

    private final BlogImageStorage blogImageStorage;

    @PostMapping("blog")
    public Result<?> uploadImage(@RequestParam("file") MultipartFile image) {
        try {
            String ref = blogImageStorage.store(image);
            log.debug("文件上传成功，{}", ref);
            return Result.ok(ref);
        } catch (IOException e) {
            throw new BizException("文件上传失败", e);
        }
    }

    @GetMapping("/blog/delete")
    public Result<?> deleteBlogImg(@RequestParam("name") String filename) {
        blogImageStorage.delete(filename);
        return Result.ok();
    }
}
