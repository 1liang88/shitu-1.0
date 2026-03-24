package com.liang.shituapi.service;

import com.liang.shituapi.config.AmapConfig;
import com.liang.shituapi.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * API配额管理服务（简化版）
 */
@Slf4j
@Service
public class ApiQuotaService {

    @Autowired
    private AmapConfig amapConfig;

    // API名称常量
    public static final String API_STATIC_MAP = "staticmap";
    public static final String API_GEOCODE = "geocode";

    // 使用内存缓存记录每日使用次数
    private final Map<String, AtomicInteger> quotaCache = new ConcurrentHashMap<>();

    /**
     * 检查API配额
     * @param apiName API名称
     * @return true表示可以继续调用
     */
    public boolean checkQuota(String apiName) {
        String key = getTodayKey(apiName);
        AtomicInteger counter = quotaCache.computeIfAbsent(key, k -> new AtomicInteger(0));

        int used = counter.get();
        int limit = getApiLimit(apiName);

        if (used >= limit) {
            log.warn("API配额已用完: {}, 今日已用: {}/{}", apiName, used, limit);
            return false;
        }

        return true;
    }

    /**
     * 增加API调用计数
     * @param apiName API名称
     */
    public void incrementCount(String apiName) {
        String key = getTodayKey(apiName);
        AtomicInteger counter = quotaCache.computeIfAbsent(key, k -> new AtomicInteger(0));

        int used = counter.incrementAndGet();
        int limit = getApiLimit(apiName);

        // 记录日志，但不做持久化
        if (used % 100 == 0) {  // 每100次记录一次
            log.info("API调用统计: {} 今日已用: {}/{}", apiName, used, limit);
        }
    }

    /**
     * 获取今日已用次数
     */
    public int getTodayUsed(String apiName) {
        String key = getTodayKey(apiName);
        AtomicInteger counter = quotaCache.get(key);
        return counter != null ? counter.get() : 0;
    }

    /**
     * 获取今日剩余次数
     */
    public int getTodayRemaining(String apiName) {
        return getApiLimit(apiName) - getTodayUsed(apiName);
    }

    /**
     * 获取API每日限额
     */
    private int getApiLimit(String apiName) {
        switch (apiName) {
            case API_STATIC_MAP:
                return amapConfig.getStaticMapDailyLimit();
            case API_GEOCODE:
                return amapConfig.getGeocodeDailyLimit();
            default:
                return 30000;
        }
    }

    /**
     * 生成今日的缓存键
     */
    private String getTodayKey(String apiName) {
        return apiName + "_" + LocalDate.now().toString();
    }

    /**
     * 重置今日计数（主要用于测试）
     */
    public void resetToday(String apiName) {
        String key = getTodayKey(apiName);
        quotaCache.remove(key);
        log.info("重置 {} 的今日计数", apiName);
    }
}