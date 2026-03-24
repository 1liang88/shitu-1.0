package com.liang.shituapi.service;

import com.liang.shituapi.config.AmapConfig;
import com.liang.shituapi.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 高德静态地图服务（无签名版本）
 */
@Slf4j
@Service
public class AmapStaticMapService {

    @Autowired
    private AmapConfig amapConfig;

    @Autowired
    private ApiQuotaService quotaService;

    /**
     * 获取静态地图URL
     */
    public String getStaticMapUrl(BigDecimal longitude, BigDecimal latitude, int zoom, int width, int height) {
        if (longitude == null || latitude == null) {
            throw new BusinessException("经纬度不能为空");
        }

        // 检查配额
        if (!quotaService.checkQuota(ApiQuotaService.API_STATIC_MAP)) {
            throw new BusinessException("静态地图服务今日调用次数已达上限，请明日再试");
        }

        // 构建URL
        String url = String.format(
                "%s?location=%f,%f&zoom=%d&size=%d*%d&markers=mid,0xFF0000,A:%f,%f&key=%s",
                amapConfig.getStaticMapUrl(),
                longitude, latitude,
                zoom,
                width, height,
                longitude, latitude,
                amapConfig.getKey()
        );

        // 增加计数
        quotaService.incrementCount(ApiQuotaService.API_STATIC_MAP);

        log.info("生成静态地图URL: {}", url);
        return url;
    }
}