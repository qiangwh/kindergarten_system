package com.kindergarten.system.common.cache;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 缓存 Key 工具类
 */
public final class CacheKeyUtil {

    private CacheKeyUtil() {
    }

    public static String key(Object... parts) {
        return Arrays.stream(parts)
                .map(part -> part == null ? "null" : String.valueOf(part))
                .collect(Collectors.joining(":"));
    }
}
