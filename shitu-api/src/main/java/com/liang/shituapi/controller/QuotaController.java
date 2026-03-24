package com.liang.shituapi.controller;

import com.liang.shituapi.service.ApiQuotaService;
import com.liang.shitucommon.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 配额查询控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/quota")
public class QuotaController {

    @Autowired
    private ApiQuotaService quotaService;

    /**
     * 查询指定API的配额使用情况
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> getQuotaStatus(@RequestParam String apiName) {
        Map<String, Object> result = new HashMap<>();
        result.put("apiName", apiName);
        result.put("used", quotaService.getTodayUsed(apiName));
        result.put("remaining", quotaService.getTodayRemaining(apiName));
        result.put("limit", getLimitByApiName(apiName));
        return Result.success(result);
    }

    /**
     * 查询所有API的配额使用情况
     */
    @GetMapping("/all")
    public Result<Map<String, Object>> getAllQuota() {
        Map<String, Object> result = new HashMap<>();
        result.put("staticmap", Map.of(
                "used", quotaService.getTodayUsed(ApiQuotaService.API_STATIC_MAP),
                "remaining", quotaService.getTodayRemaining(ApiQuotaService.API_STATIC_MAP),
                "limit", getLimitByApiName(ApiQuotaService.API_STATIC_MAP)
        ));
        result.put("geocode", Map.of(
                "used", quotaService.getTodayUsed(ApiQuotaService.API_GEOCODE),
                "remaining", quotaService.getTodayRemaining(ApiQuotaService.API_GEOCODE),
                "limit", getLimitByApiName(ApiQuotaService.API_GEOCODE)
        ));
        return Result.success(result);
    }

    private int getLimitByApiName(String apiName) {
        switch (apiName) {
            case ApiQuotaService.API_STATIC_MAP:
                return 30000;
            case ApiQuotaService.API_GEOCODE:
                return 30000;
            default:
                return 0;
        }
    }
}