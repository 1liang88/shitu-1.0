package com.liang.shituapi.controller;

import com.liang.shituapi.service.AmapGeocodeService;
import com.liang.shitucommon.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 地理编码控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/geocode")
public class GeocodeController {

    @Autowired
    private AmapGeocodeService geocodeService;

    /**
     * 地址转坐标
     */
    @GetMapping("/address")
    public Result<BigDecimal[]> geocode(@RequestParam String address) {
        try {
            BigDecimal[] coords = geocodeService.geocode(address);
            return Result.success(coords);
        } catch (Exception e) {
            log.error("地理编码失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量地理编码
     */
    //@PostMapping("/batch")
    //public Result<Map<String, BigDecimal[]>> batchGeocode(@RequestBody String[] addresses) {
    //    try {
    //        Map<String, BigDecimal[]> results = geocodeService.batchGeocode(addresses);
    //        return Result.success(results);
    //    } catch (Exception e) {
    //        log.error("批量地理编码失败", e);
    //        return Result.error(e.getMessage());
    //    }
    //}
}
