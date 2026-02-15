package com.lz.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * OSS Utility (Placeholder)
 */
@Component
public class OssUtil {
    private static final Logger log = LoggerFactory.getLogger(OssUtil.class);

    // TODO: Implement OSS operations
    public String upload(MultipartFile file) {
        log.info("OSS upload: {}", file.getOriginalFilename());
        return "http://oss-placeholder/" + file.getOriginalFilename();
    }
}
