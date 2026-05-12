package com.virgo.service.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface BlogImageStorage {

    String store(MultipartFile image) throws IOException;

    void delete(String storedRef);
}
