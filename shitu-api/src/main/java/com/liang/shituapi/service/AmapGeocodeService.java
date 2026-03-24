package com.liang.shituapi.service;

import com.liang.shituapi.config.AmapConfig;
import com.liang.shituapi.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class AmapGeocodeService {

    @Autowired
    private AmapConfig amapConfig;

    @Autowired
    private ApiQuotaService quotaService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public BigDecimal[] geocode(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new BusinessException("地址不能为空");
        }

        if (!quotaService.checkQuota(ApiQuotaService.API_GEOCODE)) {
            throw new BusinessException("地理编码服务今日调用次数已达上限，请明日再试");
        }

        HttpURLConnection conn = null;
        BufferedReader reader = null;
        try {
            // 1. 构建和浏览器完全一致的URL
            String encodedAddress = URLEncoder.encode(address.trim(), StandardCharsets.UTF_8);
            String urlStr = amapConfig.getGeocodeUrl()
                    + "?address=" + encodedAddress
                    + "&output=JSON"
                    + "&key=" + amapConfig.getKey();

            log.info("调用高德地理编码API: {}", urlStr);
            URL url = new URL(urlStr);

            // 2. 模拟浏览器请求（关键：设置和浏览器一致的请求头）
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            // 核心：设置User-Agent为浏览器标识，绕过高德的请求校验
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            conn.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9");
            conn.setRequestProperty("Connection", "keep-alive");

            // 3. 读取响应
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                log.error("高德API返回非200状态码：{}", responseCode);
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String response = sb.toString();
            log.debug("高德API原始响应: {}", response);

            // 4. 解析响应
            BigDecimal[] result = parseGeocodeResponse(response, address);

            // 5. 仅成功时扣减配额
            if (result != null) {
                quotaService.incrementCount(ApiQuotaService.API_GEOCODE);
                log.info("地理编码成功：{} -> 经度{}，纬度{}", address, result[0], result[1]);
            } else {
                log.warn("地理编码解析无结果，不扣减配额：{}", address);
            }

            return result;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("地理编码异常：{}", address, e);
            throw new BusinessException("地理编码失败：" + e.getMessage());
        } finally {
            // 关闭连接
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    log.error("关闭流失败", e);
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private BigDecimal[] parseGeocodeResponse(String json, String address) {
        if (json == null || json.isEmpty()) {
            log.warn("地理编码响应为空: {}", address);
            return null;
        }

        try {
            JsonNode root = objectMapper.readTree(json);
            String status = root.path("status").asText();
            if (!"1".equals(status)) {
                String info = root.path("info").asText("未知错误");
                String infocode = root.path("infocode").asText("");
                log.warn("地理编码失败: {} - {} (错误码:{})", address, info, infocode);
                return null;
            }

            JsonNode geocodes = root.path("geocodes");
            if (geocodes.isArray() && geocodes.size() > 0) {
                String location = geocodes.get(0).path("location").asText();
                if (location != null && location.contains(",")) {
                    String[] loc = location.split(",");
                    return new BigDecimal[]{
                            new BigDecimal(loc[0].trim()),
                            new BigDecimal(loc[1].trim())
                    };
                }
            }

            log.warn("地理编码无有效坐标: {}", address);
            return null;

        } catch (Exception e) {
            log.error("解析地理编码响应异常：{}", address, e);
            return null;
        }
    }
}