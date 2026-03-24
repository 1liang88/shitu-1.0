package com.liang.shituapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 高德地图配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "amap")
public class AmapConfig {
    private String key;              // 高德地图Key
    private String geocodeUrl = "https://restapi.amap.com/v3/geocode/geo";  // 地理编码API
    private String staticMapUrl = "https://restapi.amap.com/v3/staticmap";  // 静态地图API
    // API每日限额配置
    private int staticMapDailyLimit = 10000;    // 静态地图API每日限额
    private int geocodeDailyLimit = 10000;       // 地理编码API每日限额
    private int warningThreshold = 80;            // 警告阈值（百分比）


}
