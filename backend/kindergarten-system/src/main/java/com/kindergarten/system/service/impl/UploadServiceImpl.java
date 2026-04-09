package com.kindergarten.system.service.impl;

import com.kindergarten.system.common.config.R2Properties;
import com.kindergarten.system.common.exception.BusinessException;
import com.kindergarten.system.common.result.ResultCode;
import com.kindergarten.system.dto.UploadResult;
import com.kindergarten.system.service.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "application/pdf"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".webp", ".pdf"
    );

    /**
     * 文件 Magic Number（文件头签名）映射
     * 用于验证文件真实类型，防止 Content-Type 伪造
     */
    private static final Map<String, byte[][]> MAGIC_NUMBERS = Map.of(
            "image/jpeg", new byte[][]{
                    {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}
            },
            "image/png", new byte[][]{
                    {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}
            },
            "image/webp", new byte[][]{
                    // RIFF????WEBP
                    {0x52, 0x49, 0x46, 0x46}
            },
            "application/pdf", new byte[][]{
                    {0x25, 0x50, 0x44, 0x46} // %PDF
            }
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
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "上传文件不能为空");
        }
        // 1. 验证文件大小
        long maxSizeBytes = r2Properties.getMaxFileSizeMb() * 1024L * 1024L;
        if (file.getSize() > maxSizeBytes) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(),
                    "文件大小超过限制（最大 " + r2Properties.getMaxFileSizeMb() + "MB）");
        }
        // 2. 验证 Content-Type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(),
                    "不支持的文件类型，仅允许 JPG、PNG、WebP、PDF");
        }
        // 3. 验证文件后缀
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String ext = extractExtension(originalFilename).toLowerCase(Locale.ROOT);
            if (!ext.isEmpty() && !ALLOWED_EXTENSIONS.contains(ext)) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(),
                        "不支持的文件后缀，仅允许 .jpg、.jpeg、.png、.webp、.pdf");
            }
        }
        // 4. 验证文件 Magic Number（文件头签名）
        validateMagicNumber(file, contentType.toLowerCase(Locale.ROOT));
    }

    /**
     * 验证文件 Magic Number，防止 Content-Type 伪造
     */
    private void validateMagicNumber(MultipartFile file, String contentType) {
        byte[][] expectedMagics = MAGIC_NUMBERS.get(contentType);
        if (expectedMagics == null) {
            return; // 没有对应的 magic number 规则，跳过
        }

        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[12]; // 读取前12个字节
            int bytesRead = is.read(header);
            if (bytesRead < 3) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "文件内容无效");
            }

            boolean matched = false;
            for (byte[] magic : expectedMagics) {
                if (bytesRead >= magic.length && startsWith(header, magic)) {
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                log.warn("文件 Magic Number 不匹配，声明类型: {}, 文件名: {}",
                        contentType, file.getOriginalFilename());
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(),
                        "文件内容与声明类型不匹配");
            }
        } catch (IOException e) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "无法读取文件内容");
        }
    }

    /**
     * 检查 byte 数组是否以指定前缀开头
     */
    private boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private String buildObjectKey(MultipartFile file) {
        String suffix = resolveSuffix(file.getOriginalFilename(), file.getContentType());
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        return "receipts/" + datePath + "/" + UUID.randomUUID() + suffix;
    }

    /**
     * 提取文件后缀（安全方式，仅保留最后一个 . 之后的部分）
     */
    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        // 过滤路径遍历字符，只取文件名部分
        String safeName = filename.replace("\\", "/");
        int lastSlash = safeName.lastIndexOf('/');
        if (lastSlash >= 0) {
            safeName = safeName.substring(lastSlash + 1);
        }
        int dotIndex = safeName.lastIndexOf('.');
        if (dotIndex < 0) {
            return "";
        }
        return safeName.substring(dotIndex);
    }

    private String resolveSuffix(String filename, String contentType) {
        String ext = extractExtension(filename);
        if (!ext.isEmpty()) {
            return ext;
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
