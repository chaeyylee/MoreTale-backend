package com.moretale.global.service;

import com.moretale.global.exception.BusinessException;
import com.moretale.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    @Value("${file.upload.base-path:uploads}")
    private String basePath;

    @Value("${file.upload.base-url:http://localhost:8080/uploads}")
    private String baseUrl;

    // 파일 업로드 (File 객체)
    public String uploadFile(File file, String subPath) {
        try {
            Path targetPath = createDirectoriesAndGetPath(subPath);
            Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = generateUrl(subPath);
            log.info("파일 업로드 완료 - 실제 경로: {}, 접근 URL: {}",
                    targetPath.toAbsolutePath(), fileUrl);

            return fileUrl;
        } catch (IOException e) {
            log.error("File upload failed: {}", e.getMessage());
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    // 파일 업로드 (MultipartFile)
    public String uploadFile(MultipartFile file, String subPath) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        try {
            Path targetPath = createDirectoriesAndGetPath(subPath);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = generateUrl(subPath);
            log.info("파일 업로드 완료 - 실제 경로: {}, 접근 URL: {}",
                    targetPath.toAbsolutePath(), fileUrl);

            return fileUrl;
        } catch (IOException e) {
            log.error("MultipartFile upload failed: {}", e.getMessage());
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    // 바이트 배열로 파일 저장 (TTS 음성 데이터 저장 시 주로 사용)
    public String uploadFile(byte[] data, String subPath) {
        try {
            Path targetPath = createDirectoriesAndGetPath(subPath);
            Files.write(targetPath, data);

            String fileUrl = generateUrl(subPath);
            log.info("파일 저장 완료 - 실제 경로: {}, 접근 URL: {}",
                    targetPath.toAbsolutePath(), fileUrl);

            return fileUrl;
        } catch (IOException e) {
            log.error("Byte array upload failed: {}", e.getMessage());
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    // 파일 삭제
    public void deleteFile(String subPath) {
        try {
            Path filePath = Paths.get(basePath, subPath);
            if (Files.deleteIfExists(filePath)) {
                log.info("파일 삭제 완료: {}", subPath);
            }
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", subPath, e);
            // 삭제 실패는 핵심 로직을 멈추지 않도록 로그만 남깁니다.
        }
    }

    // 랜덤 파일명 생성
    public String generateRandomFileName(String originalFileName, String prefix) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String timestamp = String.valueOf(System.currentTimeMillis());

        return String.format("%s_%s_%s%s", prefix, timestamp, uuid, extension);
    }

    // 디렉토리 생성 및 전체 경로 반환
    private Path createDirectoriesAndGetPath(String subPath) throws IOException {
        // 절대 경로로 변환
        Path absoluteBasePath = Paths.get(basePath).toAbsolutePath();
        Path targetPath = absoluteBasePath.resolve(subPath);

        // 부모 디렉토리 생성
        Path parentDir = targetPath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
            log.info("디렉토리 생성: {}", parentDir);
        }

        return targetPath;
    }

    // 파일 접근 URL 생성
    private String generateUrl(String subPath) {
        return baseUrl + "/" + subPath;
    }
}
