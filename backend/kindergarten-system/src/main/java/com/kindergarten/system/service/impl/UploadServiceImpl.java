package com.kindergarten.system.service.impl;

import com.kindergarten.system.common.config.R2Properties;
import com.kindergarten.system.common.exception.BusinessException;
import com.kindergarten.system.common.result.ResultCode;
import com.kindergarten.system.dto.UploadResult;
import com.kindergarten.system.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "application/pdf"
    );

    private final S3Client s3Client;
    private final R2Properties r2Properties;

    @Override
    public UploadResult uploadReceipt(MultipartFile file) {
        validateFile(file);
        String key = buildObjectKey(file);
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(r2Properties.getBucket())
                .key(key)
                .contentType(file.getContentType())
                .build();
        try {
            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
        } catch (IOException e) {
            throw new BusinessException(ResultCode.UPLOAD_ERROR);
        }
        UploadResult result = new UploadResult();
        result.setKey(key);
        result.setUrl(buildPublicUrl(key));
        return result;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        long maxSizeBytes = r2Properties.getMaxFileSizeMb() * 1024L * 1024L;
        if (file.getSize() > maxSizeBytes) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    private String buildObjectKey(MultipartFile file) {
        String suffix = resolveSuffix(file.getOriginalFilename(), file.getContentType());
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        return "receipts/" + datePath + "/" + UUID.randomUUID() + suffix;
    }

    private String resolveSuffix(String filename, String contentType) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf('.'));
        }
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "application/pdf" -> ".pdf";
            default -> "";
        };
    }

    private String buildPublicUrl(String key) {
        String base = r2Properties.getPublicUrl();
        if (base == null || base.isBlank()) {
            return key;
        }
        String trimmedBase = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        String trimmedKey = key.startsWith("/") ? key.substring(1) : key;
        return trimmedBase + "/" + trimmedKey;
    }
}