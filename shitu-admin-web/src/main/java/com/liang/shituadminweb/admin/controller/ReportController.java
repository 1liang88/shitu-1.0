package com.liang.shituadminweb.admin.controller;

import com.liang.shitucommon.PageResult;
import com.liang.shitucommon.Result;
import com.liang.shitucommon.entity.Category;
import com.liang.shitucommon.entity.Report;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 上报审核控制器
 */
@Slf4j
@Controller
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.base-url}")
    private String apiBaseUrl;

    /**
     * 上报列表页面
     */
    @GetMapping
    public String list(@RequestParam(required = false) Integer pageNum,
                       @RequestParam(required = false) Integer pageSize,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer categoryId,
                       @RequestParam(required = false) Integer reportType,
                       @RequestParam(required = false) Integer status,
                       Model model) {

        try {
            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("pageNum", pageNum != null ? pageNum : 1);
            params.put("pageSize", pageSize != null ? pageSize : 10);
            params.put("keyword", keyword);
            params.put("categoryId", categoryId);
            params.put("reportType", reportType);
            params.put("status", status);

            // 构建URL
            StringBuilder url = new StringBuilder(apiBaseUrl + "/report/page");
            url.append("?pageNum=").append(params.get("pageNum"))
                    .append("&pageSize=").append(params.get("pageSize"));
            if (keyword != null && !keyword.isEmpty()) {
                url.append("&keyword=").append(keyword);
            }
            if (categoryId != null) {
                url.append("&categoryId=").append(categoryId);
            }
            if (reportType != null) {
                url.append("&reportType=").append(reportType);
            }
            if (status != null) {
                url.append("&status=").append(status);
            }

            // 调用API获取上报列表
            ParameterizedTypeReference<Result<PageResult<Report>>> typeRef =
                    new ParameterizedTypeReference<Result<PageResult<Report>>>() {};

            ResponseEntity<Result<PageResult<Report>>> response = restTemplate.exchange(
                    url.toString(),
                    HttpMethod.GET,
                    null,
                    typeRef
            );

            if (response.getBody() != null && response.getBody().isSuccess()) {
                model.addAttribute("page", response.getBody().getData());
            }

            // 获取分类列表（用于筛选）
            String categoryUrl = apiBaseUrl + "/category/list";
            ParameterizedTypeReference<Result<List<Category>>> categoryTypeRef =
                    new ParameterizedTypeReference<Result<List<Category>>>() {};

            ResponseEntity<Result<List<Category>>> categoryResponse = restTemplate.exchange(
                    categoryUrl,
                    HttpMethod.GET,
                    null,
                    categoryTypeRef
            );

            if (categoryResponse.getBody() != null && categoryResponse.getBody().isSuccess()) {
                model.addAttribute("categories", categoryResponse.getBody().getData());
            }

        } catch (Exception e) {
            log.error("获取上报列表失败", e);
            model.addAttribute("error", "获取上报列表失败：" + e.getMessage());
        }

        // 保留查询条件
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("reportType", reportType);
        model.addAttribute("status", status);
        model.addAttribute("menu", "report");
        model.addAttribute("title", "上报审核");

        return "report/list";
    }

    /**
     * 查看上报详情
     */
    @GetMapping("/view/{id}")
    public String viewPage(@PathVariable Integer id, Model model) {
        try {
            String url = apiBaseUrl + "/report/" + id;
            ParameterizedTypeReference<Result<Report>> typeRef =
                    new ParameterizedTypeReference<Result<Report>>() {};

            ResponseEntity<Result<Report>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    typeRef
            );

            if (response.getBody() != null && response.getBody().isSuccess()) {
                model.addAttribute("report", response.getBody().getData());
            }
        } catch (Exception e) {
            log.error("获取上报详情失败", e);
            model.addAttribute("error", "获取上报详情失败：" + e.getMessage());
        }

        model.addAttribute("menu", "report");
        model.addAttribute("title", "上报详情");

        return "report/view";
    }

    /**
     * 审核上报
     */
    @PostMapping("/audit/{id}")
    @ResponseBody
    public Result<String> audit(@PathVariable Integer id,
                              @RequestParam Integer status,
                              @RequestParam(required = false) String remark,
                              HttpSession session) {
        try {
            Integer adminId = (Integer) session.getAttribute("adminId");
            if (adminId == null) {
                return Result.unauthorized("请先登录");
            }

            String url = apiBaseUrl + "/report/" + id + "/audit?status=" + status +
                    (remark != null ? "&remark=" + remark : "");
            restTemplate.put(url, null);

            String message = status == 1 ? "审核通过" : "已驳回";
            return Result.success(message);
        } catch (Exception e) {
            log.error("审核失败", e);
            return Result.error("审核失败：" + e.getMessage());
        }
    }

    /**
     * 批量审核
     */
    @PostMapping("/batch-audit")
    @ResponseBody
    public Result<String> batchAudit(@RequestBody List<Integer> ids,
                                   @RequestParam Integer status,
                                   @RequestParam(required = false) String remark,
                                   HttpSession session) {
        try {
            Integer adminId = (Integer) session.getAttribute("adminId");
            if (adminId == null) {
                return Result.unauthorized("请先登录");
            }

            String url = apiBaseUrl + "/report/batch/audit?status=" + status;
            restTemplate.put(url, ids);

            String message = status == 1 ? "批量通过成功" : "批量驳回成功";
            return Result.success(message);
        } catch (Exception e) {
            log.error("批量审核失败", e);
            return Result.error("批量审核失败：" + e.getMessage());
        }
    }

    /**
     * 删除上报记录
     */
    @PostMapping("/delete/{id}")
    @ResponseBody
    public Result<String> delete(@PathVariable Integer id) {
        try {
            String url = apiBaseUrl + "/report/" + id;
            restTemplate.delete(url);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}
