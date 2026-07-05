package com.quiz.platform.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

public interface MinioStorageService {

    /** 上传文件，返回在MinIO中的对象路径 */
    String upload(MultipartFile file, String objectPrefix);

    InputStream download(String objectPath);
}
