package com.lz.util;

import com.lz.config.FallbackConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 图片工具类，提供兜底逻辑
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImageUtils {

    private final FallbackConfig fallbackConfig;

    /**
     * 获取随机兜底图片URL
     *
     * @return 图片URL
     */
    public String getRandomFallbackUrl() {
        List<String> urls = fallbackConfig.getUrls();
        if (urls == null || urls.isEmpty()) {
            log.warn("Fallback image URLs are not configured!");
            return null;
        }
        
        // 使用 ThreadLocalRandom 保证并发性能
        int index = ThreadLocalRandom.current().nextInt(urls.size());
        String selectedUrl = urls.get(index);
        
        log.debug("Selected fallback image: {}", selectedUrl);
        return selectedUrl;
    }

    /**
     * 如果传入的URL为空，则返回兜底URL
     * 
     * @param originalUrl 原始URL
     * @return 原URL或兜底URL
     */
    public String getUrlOrDefault(String originalUrl) {
        if (originalUrl != null && !originalUrl.trim().isEmpty()) {
            return originalUrl;
        }
        return getRandomFallbackUrl();
    }
}
