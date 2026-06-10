package com.moretale.global.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Arrays;

@Hidden
@RestController
public class FileTestController {

    @Value("${file.upload.base-path:uploads}")
    private String basePath;

    @Value("${file.upload.base-url:http://localhost:8080/uploads}")
    private String baseUrl;

    @GetMapping("/api/test/file-path")
    public Map<String, Object> testFilePath() {
        Map<String, Object> result = new HashMap<>();

        Path absolutePath = Paths.get(basePath).toAbsolutePath();
        result.put("configuredPath", basePath);
        result.put("absolutePath", absolutePath.toString());
        result.put("exists", Files.exists(absolutePath));
        result.put("isDirectory", Files.isDirectory(absolutePath));
        result.put("baseUrl", baseUrl);

        File audioDir = new File(absolutePath.toFile(), "tts/audio");
        result.put("audioDirPath", audioDir.getAbsolutePath());
        result.put("audioDirExists", audioDir.exists());

        if (audioDir.exists()) {
            File[] files = audioDir.listFiles();
            if (files != null && files.length > 0) {
                result.put("fileCount", files.length);
                result.put("files", Arrays.stream(files)
                        .map(File::getName)
                        .collect(Collectors.toList()));
            } else {
                result.put("fileCount", 0);
            }
        }

        return result;
    }
}
