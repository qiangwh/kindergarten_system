package com.kindergarten.system.service;

import com.kindergarten.system.dto.UploadResult;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {

    UploadResult uploadReceipt(MultipartFile file);
}