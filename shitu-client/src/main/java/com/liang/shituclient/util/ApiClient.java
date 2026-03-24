package com.liang.shituclient.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.liang.shitucommon.Result;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * API客户端工具类（使用原生HTTP请求）
 */
public class ApiClient {
    private static final Logger LOGGER = Logger.getLogger(ApiClient.class.getName());

    private static String baseUrl = "http://localhost:8080/api/v1";

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static void setBaseUrl(String url) {
        baseUrl = url;
    }

    // ==================== GET请求 ====================

    public static <T> Result<T> get(String path, Class<T> responseType) {
        return get(path, null, new TypeReference<Result<T>>() {});
    }

    public static <T> Result<T> get(String path, TypeReference<Result<T>> typeRef) {
        return get(path, null, typeRef);
    }

    public static <T> Result<T> get(String path, Map<String, String> params, TypeReference<Result<T>> typeRef) {
        HttpURLConnection connection = null;
        try {
            String url = buildUrl(path, params);
            LOGGER.info("请求URL: " + url);

            URL requestUrl = new URL(url);
            connection = (HttpURLConnection) requestUrl.openConnection();

            // 设置请求方法
            connection.setRequestMethod("GET");

            // 设置超时
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            // 设置请求头 - 模拟浏览器
            connection.setRequestProperty("Accept", "application/json, text/plain, */*");
            connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            connection.setRequestProperty("Connection", "keep-alive");

            // 连接
            connection.connect();

            // 检查响应码
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                LOGGER.warning("HTTP请求失败，响应码: " + responseCode);
                return Result.error("HTTP请求失败，响应码: " + responseCode);
            }

            // 读取响应
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            String responseBody = response.toString();
            LOGGER.fine("API响应: " + responseBody);

            // 使用 TypeReference 正确反序列化
            return objectMapper.readValue(responseBody, typeRef);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "API请求异常", e);
            return Result.error("API请求异常：" + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // ==================== POST请求 ====================

    public static <T> Result<T> post(String path, Object body, Class<T> responseType) {
        return post(path, body, new TypeReference<Result<T>>() {});
    }

    public static <T> Result<T> post(String path, Object body, TypeReference<Result<T>> typeRef) {
        HttpURLConnection connection = null;
        try {
            String url = baseUrl + path;
            LOGGER.info("请求URL: " + url);

            URL requestUrl = new URL(url);
            connection = (HttpURLConnection) requestUrl.openConnection();

            // 设置请求方法
            connection.setRequestMethod("POST");

            // 设置超时
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            // 设置请求头 - 模拟浏览器
            connection.setRequestProperty("Accept", "application/json, text/plain, */*");
            connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("Connection", "keep-alive");

            // 允许输出
            connection.setDoOutput(true);

            // 写入请求体
            if (body != null) {
                String jsonBody = objectMapper.writeValueAsString(body);
                LOGGER.fine("请求体: " + jsonBody);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }

            // 检查响应码
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                LOGGER.warning("HTTP请求失败，响应码: " + responseCode);
                return Result.error("HTTP请求失败，响应码: " + responseCode);
            }

            // 读取响应
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            String responseBody = response.toString();
            LOGGER.fine("API响应: " + responseBody);

            return objectMapper.readValue(responseBody, typeRef);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "API请求异常", e);
            return Result.error("API请求异常：" + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // ==================== PUT请求 ====================

    public static <T> Result<T> put(String path, Object body, Class<T> responseType) {
        return put(path, body, new TypeReference<Result<T>>() {});
    }

    public static <T> Result<T> put(String path, Object body, TypeReference<Result<T>> typeRef) {
        HttpURLConnection connection = null;
        try {
            String url = baseUrl + path;
            LOGGER.info("请求URL: " + url);

            URL requestUrl = new URL(url);
            connection = (HttpURLConnection) requestUrl.openConnection();

            // 设置请求方法
            connection.setRequestMethod("PUT");

            // 设置超时
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            // 设置请求头
            connection.setRequestProperty("Accept", "application/json, text/plain, */*");
            connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("Connection", "keep-alive");

            // 允许输出
            connection.setDoOutput(true);

            // 写入请求体
            if (body != null) {
                String jsonBody = objectMapper.writeValueAsString(body);
                LOGGER.fine("请求体: " + jsonBody);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                LOGGER.warning("HTTP请求失败，响应码: " + responseCode);
                return Result.error("HTTP请求失败，响应码: " + responseCode);
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            String responseBody = response.toString();
            LOGGER.fine("API响应: " + responseBody);

            return objectMapper.readValue(responseBody, typeRef);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "API请求异常", e);
            return Result.error("API请求异常：" + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // ==================== DELETE请求 ====================

    public static <T> Result<T> delete(String path, Class<T> responseType) {
        return delete(path, new TypeReference<Result<T>>() {});
    }

    public static <T> Result<T> delete(String path, TypeReference<Result<T>> typeRef) {
        HttpURLConnection connection = null;
        try {
            String url = baseUrl + path;
            LOGGER.info("请求URL: " + url);

            URL requestUrl = new URL(url);
            connection = (HttpURLConnection) requestUrl.openConnection();

            // 设置请求方法
            connection.setRequestMethod("DELETE");

            // 设置超时
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            // 设置请求头
            connection.setRequestProperty("Accept", "application/json, text/plain, */*");
            connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            connection.setRequestProperty("Connection", "keep-alive");

            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                LOGGER.warning("HTTP请求失败，响应码: " + responseCode);
                return Result.error("HTTP请求失败，响应码: " + responseCode);
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            String responseBody = response.toString();
            LOGGER.fine("API响应: " + responseBody);

            return objectMapper.readValue(responseBody, typeRef);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "API请求异常", e);
            return Result.error("API请求异常：" + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // ==================== 私有方法 ====================

    private static String buildUrl(String path, Map<String, String> params) {
        StringBuilder url = new StringBuilder(baseUrl).append(path);
        if (params != null && !params.isEmpty()) {
            url.append("?");
            params.forEach((key, value) -> {
                try {
                    url.append(key).append("=")
                            .append(URLEncoder.encode(value, StandardCharsets.UTF_8))
                            .append("&");
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "URL编码失败", e);
                }
            });
            url.deleteCharAt(url.length() - 1);
        }
        return url.toString();
    }
}