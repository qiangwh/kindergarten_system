package com.kindergarten.system.controller;

import com.kindergarten.system.common.result.Result;
import com.kindergarten.system.dto.UploadResult;
import com.kindergarten.system.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 * <p>
 * 提供图片上传相关接口。
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    /** 上传服务 */
    private final UploadService uploadService;

    /**
     * 上传收据/图片
     *
     * @param file 图片文件
     * @return 上传结果
     */
    @PostMapping("/image")
    public Result<UploadResult> uploadReceipt(@RequestParam("file") MultipartFile file) {
        return Result.success(uploadService.uploadReceipt(file));
    }
}