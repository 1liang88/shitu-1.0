package com.liang.shituapi.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

/**
 * 高德地图数字签名工具类
 * 官方文档：https://lbs.amap.com/api/webservice/guide/create-project/get-key
 */
@Slf4j
@Component
public class AmapSignatureUtil {

    /**
     * 生成数字签名（高德地图官方标准）
     * @param params 请求参数
     * @param secretKey 密钥
     * @return 签名
     */
    public static String generateSignature(Map<String, String> params, String secretKey) {
        // 1. 对参数按key进行升序排序
        TreeMap<String, String> sortedParams = new TreeMap<>(params);

        // 2. 拼接参数
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value != null && !value.isEmpty()) {
                sb.append(key).append("=").append(value).append("&");
            }
        }

        // 3. 去掉最后一个&
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }

        // 4. 在拼接的字符串末尾加上密钥
        sb.append(secretKey);

        // 5. 生成MD5签名
        String sign = DigestUtils.md5DigestAsHex(sb.toString().getBytes(StandardCharsets.UTF_8));
        log.debug("生成的签名: {}", sign);

        return sign;
    }

    /**
     * 构建带签名的URL
     */
    public static String buildSignedUrl(String baseUrl, Map<String, String> params,
                                        String key, String secretKey) {
        // 创建参数副本
        Map<String, String> signParams = new TreeMap<>(params);

        // 添加key参数
        signParams.put("key", key);

        // 生成签名
        String sign = generateSignature(signParams, secretKey);

        // 构建URL
        StringBuilder url = new StringBuilder(baseUrl).append("?");
        try {
            for (Map.Entry<String, String> entry : signParams.entrySet()) {
                url.append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), "UTF-8"))
                        .append("&");
            }
            // 添加签名参数
            url.append("sig=").append(sign);
        } catch (UnsupportedEncodingException e) {
            log.error("URL编码失败", e);
        }

        return url.toString();
    }
}