package com.kindergarten.system.common.cache;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 缓存 Key 工具类。
 * <p>
 * 统一用冒号拼接多个 key 片段，便于阅读、排查和后续扩展。
 * </p>
 */
public final class CacheKeyUtil {

    private CacheKeyUtil() {
    }

    public static String key(Object... parts) {
        // 允许传入 null，避免拼接 key 时因为参数缺失导致异常。
        return Arrays.stream(parts)
                .map(part -> part == null ? "null" : String.valueOf(part))
                .collect(Collectors.joining(":"));
    }
}
