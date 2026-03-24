package com.liang.shituadminweb.admin.controller;

import com.liang.shitucommon.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/report")
public class ReportApiController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.base-url}")
    private String apiBaseUrl;

    /**
     * 获取待处理上报数量
     */
    @GetMapping("/pending/count")
    public Result<Long> getPendingCount() {
        try {
            String url = apiBaseUrl + "/report/pending/count";

            ParameterizedTypeReference<Result<Long>> typeRef =
                    new ParameterizedTypeReference<Result<Long>>() {};

            ResponseEntity<Result<Long>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, typeRef
            );

            if (response.getBody() != null && response.getBody().isSuccess()) {
                return Result.success(response.getBody().getData());
            } else {
                return Result.success(0L);
            }
        } catch (Exception e) {
            log.error("获取待处理数量失败", e);
            return Result.success(0L);
        }
    }

    /**
     * 审核上报 - 将管理员ID作为参数传递
     */
    @PutMapping("/audit/{id}")
    public Result<String> audit(@PathVariable Integer id,
                                @RequestParam Integer status,
                                @RequestParam(required = false) String remark,
                                HttpServletRequest request) {
        try {
            // 从Web管理端的session获取管理员ID
            Integer adminId = (Integer) request.getSession().getAttribute("adminId");
            if (adminId == null) {
                log.error("管理员未登录");
                return Result.unauthorized("请先登录");
            }

            log.info("转发审核请求，ID: {}, 状态: {}, 审核人: {}", id, status, adminId);

            // 构建后端API URL，将adminId作为参数传递
            StringBuilder url = new StringBuilder(apiBaseUrl + "/report/" + id + "/audit");
            url.append("?status=").append(status);
            url.append("&adminId=").append(adminId);  // 传递管理员ID
            if (remark != null && !remark.isEmpty()) {
                url.append("&remark=").append(URLEncoder.encode(remark, StandardCharsets.UTF_8.toString()));
            }

            log.info("转发到后端API: {}", url);

            // 发送 PUT 请求
            ParameterizedTypeReference<Result<String>> typeRef =
                    new ParameterizedTypeReference<Result<String>>() {};

            ResponseEntity<Result<String>> response = restTemplate.exchange(
                    url.toString(),
                    HttpMethod.PUT,
                    null,
                    typeRef
            );

            if (response.getBody() != null) {
                return response.getBody();
            } else {
                return Result.error("审核失败：后端无响应");
            }
        } catch (Exception e) {
            log.error("审核请求转发失败", e);
            return Result.error("审核失败：" + e.getMessage());
        }
    }

    /**
     * 批量审核
     */
    @PutMapping("/batch/audit")
    public Result<String> batchAudit(@RequestBody List<Integer> ids,
                                     @RequestParam Integer status,
                                     @RequestParam(required = false) String remark,
                                     HttpServletRequest request) {
        try {
            Integer adminId = (Integer) request.getSession().getAttribute("adminId");
            if (adminId == null) {
                return Result.unauthorized("请先登录");
            }

            log.info("转发批量审核请求，数量: {}, 状态: {}, 审核人: {}", ids.size(), status, adminId);

            StringBuilder url = new StringBuilder(apiBaseUrl + "/report/batch/audit");
            url.append("?status=").append(status);
            url.append("&adminId=").append(adminId);
            if (remark != null && !remark.isEmpty()) {
                url.append("&remark=").append(URLEncoder.encode(remark, StandardCharsets.UTF_8.toString()));
            }

            HttpEntity<List<Integer>> requestEntity = new HttpEntity<>(ids);

            ParameterizedTypeReference<Result<String>> typeRef =
                    new ParameterizedTypeReference<Result<String>>() {};

            ResponseEntity<Result<String>> response = restTemplate.exchange(
                    url.toString(),
                    HttpMethod.PUT,
                    requestEntity,
                    typeRef
            );

            if (response.getBody() != null) {
                return response.getBody();
            } else {
                return Result.error("批量审核失败：后端无响应");
            }
        } catch (Exception e) {
            log.error("批量审核请求转发失败", e);
            return Result.error("批量审核失败：" + e.getMessage());
        }
    }
}