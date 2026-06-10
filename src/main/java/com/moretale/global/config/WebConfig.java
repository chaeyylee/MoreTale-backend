package com.moretale.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.base-path:uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 절대 경로로 변환
        Path absolutePath = Paths.get(uploadPath).toAbsolutePath();

        // Windows 경로를 URL 형식으로 변환 (역슬래시 -> 슬래시)
        String normalizedPath = absolutePath.toString().replace("\\", "/");

        // file:/// 프로토콜 추가
        String fileLocation = "file:///" + normalizedPath + "/";

        // 업로드된 파일을 HTTP로 접근 가능하도록 설정
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(fileLocation);

        System.out.println("======================================");
        System.out.println("Static Resource Mapping Configured:");
        System.out.println("  URL Pattern: /uploads/**");
        System.out.println("  File Location: " + fileLocation);
        System.out.println("======================================");
    }
}
