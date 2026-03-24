package com.liang.shituadminweb.admin.controller;

import com.liang.shituadminweb.admin.util.ApiClient;
import com.liang.shitucommon.Result;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 地理编码API控制器（供Web端AJAX调用）
 */
@Slf4j
@RestController
@RequestMapping("/api/geocode")
public class GeocodeApiController {

    @Autowired
    private ApiClient apiClient;

    /**
     * 地址转坐标
     */
    @GetMapping("/address")
    public Result<BigDecimal[]> geocode(@RequestParam String address) {
        log.info("收到地理编码请求，地址：{}", address);
        try {
            // 调用后端API获取坐标
            Result<BigDecimal[]> result = apiClient.get(
                    "/geocode/address?address=" + address,
                    new TypeReference<Result<BigDecimal[]>>() {});

            if (result != null && result.isSuccess()) {
                log.info("坐标获取成功：{}", (Object) result.getData());
            } else {
                log.warn("坐标获取失败：{}", result != null ? result.getMessage() : "未知错误");
            }

            return result;
        } catch (Exception e) {
            log.error("获取坐标异常", e);
            return Result.error("获取坐标失败：" + e.getMessage());
        }
    }
}