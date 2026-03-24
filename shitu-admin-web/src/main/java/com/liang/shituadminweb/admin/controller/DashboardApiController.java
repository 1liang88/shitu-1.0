package com.liang.shituadminweb.admin.controller;

import com.liang.shitucommon.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘API控制器（返回JSON给前端AJAX调用）
 */
@Slf4j
@RestController
@RequestMapping("/api/dashboard")
public class DashboardApiController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.base-url}")
    private String apiBaseUrl;

    /**
     * 获取统计数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        try {
            // 调用API获取点位分类统计
            String categoryUrl = apiBaseUrl + "/place/stats/category";
            ParameterizedTypeReference<Result<List<Map<String, Object>>>> categoryTypeRef =
                    new ParameterizedTypeReference<Result<List<Map<String, Object>>>>() {};

            ResponseEntity<Result<List<Map<String, Object>>>> categoryResponse = restTemplate.exchange(
                    categoryUrl, HttpMethod.GET, null, categoryTypeRef
            );

            // 调用API获取上报类型统计
            String reportUrl = apiBaseUrl + "/report/stats/type";
            ParameterizedTypeReference<Result<List<Map<String, Object>>>> reportTypeRef =
                    new ParameterizedTypeReference<Result<List<Map<String, Object>>>>() {};

            ResponseEntity<Result<List<Map<String, Object>>>> reportResponse = restTemplate.exchange(
                    reportUrl, HttpMethod.GET, null, reportTypeRef
            );

            // 调用API获取待处理数量
            String pendingUrl = apiBaseUrl + "/report/pending/count";
            ParameterizedTypeReference<Result<Long>> pendingTypeRef =
                    new ParameterizedTypeReference<Result<Long>>() {};

            ResponseEntity<Result<Long>> pendingResponse = restTemplate.exchange(
                    pendingUrl, HttpMethod.GET, null, pendingTypeRef
            );

            // 组装返回数据
            Map<String, Object> stats = new HashMap<>();

            if (categoryResponse.getBody() != null && categoryResponse.getBody().isSuccess()) {
                stats.put("placeByCategory", categoryResponse.getBody().getData());
            }

            if (reportResponse.getBody() != null && reportResponse.getBody().isSuccess()) {
                stats.put("reportByType", reportResponse.getBody().getData());
            }

            if (pendingResponse.getBody() != null && pendingResponse.getBody().isSuccess()) {
                stats.put("reportPending", pendingResponse.getBody().getData());
            }

            // 模拟一些数据（实际应该从多个API汇总）
            stats.put("totalPlaces", 156);
            stats.put("totalReports", 89);
            stats.put("totalCategories", 5);

            return Result.success(stats);

        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            return Result.error("获取统计数据失败");
        }
    }

    /**
     * 获取热门区域
     */
    @GetMapping("/hot-areas")
    public Result<List<Map<String, Object>>> getHotAreas() {
        try {
            String url = apiBaseUrl + "/place/stats/hot-area?limit=10";

            ParameterizedTypeReference<Result<List<Map<String, Object>>>> typeRef =
                    new ParameterizedTypeReference<Result<List<Map<String, Object>>>>() {};

            ResponseEntity<Result<List<Map<String, Object>>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, typeRef
            );

            if (response.getBody() != null && response.getBody().isSuccess()) {
                return Result.success(response.getBody().getData());
            } else {
                return Result.error("获取热门区域失败");
            }
        } catch (Exception e) {
            log.error("获取热门区域失败", e);
            return Result.error("获取热门区域失败");
        }
    }
}