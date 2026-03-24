package com.liang.shituadminweb.admin.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.liang.shitucommon.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Web管理端API客户端工具类
 * 用于调用后端API服务（shitu-api）
 */
@Slf4j
@Component  // 添加这个注解让Spring管理
public class ApiClient {

    @Value("${api.base-url:http://localhost:8080/api/v1}")
    private String baseUrl;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * GET请求
     */
    public <T> Result<T> get(String path, Class<T> responseType) {
        return get(path, null, new TypeReference<Result<T>>() {});
    }

    /**
     * GET请求（支持复杂泛型）
     */
    public <T> Result<T> get(String path, TypeReference<Result<T>> typeRef) {
        return get(path, null, typeRef);
    }

    /**
     * GET请求（带参数）
     */
    public <T> Result<T> get(String path, Map<String, String> params, TypeReference<Result<T>> typeRef) {
        try {
            String url = buildUrl(path, params);
            log.debug("调用后端API: {}", url);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new java.net.URI(url))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), typeRef);
            } else {
                log.error("API请求失败，状态码：{}，响应：{}", response.statusCode(), response.body());
                return Result.error("API请求失败：" + response.statusCode());
            }
        } catch (Exception e) {
            log.error("API请求异常", e);
            return Result.error("API请求异常：" + e.getMessage());
        }
    }

    /**
     * POST请求
     */
    public <T> Result<T> post(String path, Object body, TypeReference<Result<T>> typeRef) {
        try {
            String url = baseUrl + path;
            log.debug("调用后端API: {}", url);

            String jsonBody = objectMapper.writeValueAsString(body);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new java.net.URI(url))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), typeRef);
            } else {
                log.error("API请求失败，状态码：{}，响应：{}", response.statusCode(), response.body());
                return Result.error("API请求失败：" + response.statusCode());
            }
        } catch (Exception e) {
            log.error("API请求异常", e);
            return Result.error("API请求异常：" + e.getMessage());
        }
    }

    /**
     * 构建URL
     */
    private String buildUrl(String path, Map<String, String> params) {
        StringBuilder url = new StringBuilder(baseUrl).append(path);
        if (params != null && !params.isEmpty()) {
            url.append("?");
            params.forEach((key, value) -> {
                try {
                    url.append(key).append("=")
                            .append(URLEncoder.encode(value, StandardCharsets.UTF_8))
                            .append("&");
                } catch (Exception e) {
                    log.warn("URL编码失败", e);
                }
            });
            url.deleteCharAt(url.length() - 1);
        }
        return url.toString();
    }
}