package com.andmark.quotegen.service.webimpl;

import com.andmark.quotegen.service.WebVersionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Service
@Slf4j
public class WebVersionServiceImpl implements WebVersionService {

    @Override
    public String getReadmeContent() {
        return loadFileContent("static/version-info/readme.txt");
    }

    @Override
    public String getChangelogContent() {
        return loadFileContent("static/version-info/CHANGELOG.md");
    }

    private String loadFileContent(String resourcePath) {
        log.debug("try to load file content from file: {}", resourcePath);

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream != null) {
                byte[] bytes = inputStream.readAllBytes();
                return new String(bytes, StandardCharsets.UTF_8);
            } else {
                log.error("Resource not found: {}", resourcePath);
                return "";
            }
        } catch (IOException e) {
            log.error("Error loading resource content", e);
            return "";
        }
    }
}
