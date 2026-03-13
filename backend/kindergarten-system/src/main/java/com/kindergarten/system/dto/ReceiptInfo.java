package com.kindergarten.system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 收据信息 DTO
 * <p>
 * 用于封装单张收据图片的信息，包括URL、原始文件名、上传时间等
 * 支持多张收据图片的存储和展示
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptInfo {

    /** 收据图片URL */
    private String url;

    /** 原始文件名 */
    private String fileName;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 上传时间 */
    private String uploadTime;

    /**
     * 简化构造方法（仅URL）
     *
     * @param url 收据图片URL
     */
    public ReceiptInfo(String url) {
        this.url = url;
        this.uploadTime = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 带文件名的构造方法
     *
     * @param url      收据图片URL
     * @param fileName 原始文件名
     */
    public ReceiptInfo(String url, String fileName) {
        this.url = url;
        this.fileName = fileName;
        this.uploadTime = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

}
