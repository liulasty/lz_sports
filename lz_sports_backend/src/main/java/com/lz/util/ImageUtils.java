package com.lz.util;

import com.lz.config.FallbackConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
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

        // 过滤掉 null / 空白项，避免随机到无效 URL
        List<String> candidates = urls.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toList());

        if (candidates.isEmpty()) {
            log.warn("Fallback image URLs are configured but all entries are blank!");
            return null;
        }

        // 使用 ThreadLocalRandom 保证并发性能
        int index = ThreadLocalRandom.current().nextInt(candidates.size());
        String selectedUrl = candidates.get(index);

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
        if (StringUtils.hasText(originalUrl)) {
            return originalUrl.trim();
        }
        String fallback = getRandomFallbackUrl();
        if (StringUtils.hasText(fallback)) {
            return fallback.trim();
        }

        // 兜底配置也不可用时，返回稳定默认图，避免下游 NPE/空图
        log.warn("Fallback image URL is unavailable; using default avatar instead.");
        return getDefaultAvatar();
    }

    /**
     * 对图片数组做兜底处理，确保返回指定数量的可用 URL。
     * <p>
     * 规则：
     * - 过滤 null/空白项并 trim
     * - 若数量不足，则用随机兜底图补齐
     * - 若兜底图也不可用，则用默认头像补齐
     *
     * @param originalUrls 原始图片URL列表（可为 null）
     * @param desiredCount 期望数量（<=0 时默认 3）
     * @return 始终返回非 null 列表，且 size >= 1（通常为 desiredCount）
     */
    public List<String> getImageUrlsOrFallback(List<String> originalUrls, int desiredCount) {
        int target = desiredCount > 0 ? desiredCount : 3;

        // 用 Set 去重并保序，避免重复图片造成观感问题
        Set<String> result = new LinkedHashSet<>();
        if (originalUrls != null && !originalUrls.isEmpty()) {
            for (String url : originalUrls) {
                if (StringUtils.hasText(url)) {
                    result.add(url.trim());
                }
            }
        }

        // 若多于 target，截取稳定前 N 项，避免每次请求都出现“盲盒式跳图”
        if (result.size() > target) {
            List<String> stable = new ArrayList<>(result);
            return new ArrayList<>(stable.subList(0, target));
        }

        // 不足则补齐
        int fallbackOffset = 0;
        int seed = result.stream().collect(Collectors.joining("|")).hashCode();
        while (result.size() < target) {
            String fallback = getFallbackUrlBySeed(seed, fallbackOffset++);
            if (StringUtils.hasText(fallback)) {
                result.add(fallback.trim());
            } else {
                result.add(getDefaultAvatar());
            }
        }

        return new ArrayList<>(result);
    }

    /**
     * 便捷方法：确保返回 3 张图（常用于 banner/轮播/列表封面）。
     */
    public List<String> get3ImageUrlsOrFallback(List<String> originalUrls) {
        return getImageUrlsOrFallback(originalUrls, 3);
    }

    /**
     * 获取默认头像URL
     *
     * @return 默认头像URL
     */
    public static String getDefaultAvatar() {
        return "https://lz-sports.oss-cn-beijing.aliyuncs.com/default-avatar.png";
    }

    private String getFallbackUrlBySeed(int seed, int offset) {
        List<String> urls = fallbackConfig.getUrls();
        if (urls == null || urls.isEmpty()) {
            return null;
        }
        List<String> candidates = urls.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toList());
        if (candidates.isEmpty()) {
            return null;
        }
        int base = Math.floorMod(seed, candidates.size());
        int index = (base + Math.max(0, offset)) % candidates.size();
        return candidates.get(index);
    }
}
