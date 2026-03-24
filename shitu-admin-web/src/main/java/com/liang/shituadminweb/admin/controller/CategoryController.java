package com.liang.shituadminweb.admin.controller;


import com.liang.shitucommon.PageResult;
import com.liang.shitucommon.Result;
import com.liang.shitucommon.entity.Category;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 分类管理控制器
 */
@Slf4j
@Controller
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.base-url}")
    private String apiBaseUrl;

    /**
     * 分类列表页面
     */
    @GetMapping
    public String list(@RequestParam(required = false) Integer pageNum,
                       @RequestParam(required = false) Integer pageSize,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer status,
                       Model model) {

        try {
            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("pageNum", pageNum != null ? pageNum : 1);
            params.put("pageSize", pageSize != null ? pageSize : 10);
            params.put("keyword", keyword);
            params.put("status", status);

            // 构建URL
            StringBuilder url = new StringBuilder(apiBaseUrl + "/category/page");
            url.append("?pageNum=").append(params.get("pageNum"))
                    .append("&pageSize=").append(params.get("pageSize"));
            if (keyword != null && !keyword.isEmpty()) {
                url.append("&keyword=").append(keyword);
            }
            if (status != null) {
                url.append("&status=").append(status);
            }

            // 调用API获取分类列表
            ParameterizedTypeReference<Result<PageResult<Category>>> typeRef =
                    new ParameterizedTypeReference<Result<PageResult<Category>>>() {};

            ResponseEntity<Result<PageResult<Category>>> response = restTemplate.exchange(
                    url.toString(),
                    HttpMethod.GET,
                    null,
                    typeRef
            );

            if (response.getBody() != null && response.getBody().isSuccess()) {
                model.addAttribute("page", response.getBody().getData());
            }

        } catch (Exception e) {
            log.error("获取分类列表失败", e);
            model.addAttribute("error", "获取分类列表失败：" + e.getMessage());
        }

        // 保留查询条件
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("menu", "category");
        model.addAttribute("title", "分类管理");

        return "category/list";
    }

    /**
     * 新增分类页面
     */
    @GetMapping("/add")
    public String addPage(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("menu", "category");
        model.addAttribute("title", "新增分类");
        return "category/edit";
    }

    /**
     * 编辑分类页面
     */
    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Integer id, Model model) {
        try {
            String url = apiBaseUrl + "/category/" + id;

            ParameterizedTypeReference<Result<Category>> typeRef =
                    new ParameterizedTypeReference<Result<Category>>() {};

            ResponseEntity<Result<Category>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    typeRef
            );

            if (response.getBody() != null && response.getBody().isSuccess()) {
                model.addAttribute("category", response.getBody().getData());
            }

        } catch (Exception e) {
            log.error("获取分类详情失败", e);
            model.addAttribute("error", "获取分类详情失败：" + e.getMessage());
        }

        model.addAttribute("menu", "category");
        model.addAttribute("title", "编辑分类");
        return "category/edit";
    }

    /**
     * 保存分类（新增/修改）
     */
    @PostMapping("/save")
    public String save(Category category,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        try {
            String url = apiBaseUrl + "/category";
            Map<String, Object> request = new HashMap<>();
            request.put("name", category.getName());
            request.put("code", category.getCode());
            request.put("icon", category.getIcon());
            request.put("sort", category.getSort());
            request.put("description", category.getDescription());
            request.put("status", category.getStatus() != null ? category.getStatus() : 1);

            Result<?> response;
            if (category.getId() != null) {
                // 修改
                ParameterizedTypeReference<Result<?>> typeRef =
                        new ParameterizedTypeReference<Result<?>>() {};
                ResponseEntity<Result<?>> responseEntity = restTemplate.exchange(
                        url + "/" + category.getId(),
                        HttpMethod.PUT,
                        new HttpEntity<>(request),
                        typeRef
                );
                response = responseEntity.getBody();
            } else {
                // 新增
                response = restTemplate.postForObject(url, request, Result.class);
            }

            if (response != null && response.isSuccess()) {
                redirectAttributes.addFlashAttribute("success", "保存成功");
                return "redirect:/category";
            } else {
                redirectAttributes.addFlashAttribute("error",
                        response != null ? response.getMessage() : "保存失败");
            }
        } catch (Exception e) {
            log.error("保存分类失败", e);
            redirectAttributes.addFlashAttribute("error", "保存失败：" + e.getMessage());
        }

        return "redirect:/category/edit" + (category.getId() != null ? "/" + category.getId() : "/add");
    }

    /**
     * 删除分类
     */
    @PostMapping("/delete/{id}")
    @ResponseBody
    public Result<String> delete(@PathVariable Integer id) {
        try {
            String url = apiBaseUrl + "/category/" + id;
            restTemplate.delete(url);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除分类失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 更新状态
     */
    @PostMapping("/update-status")
    @ResponseBody
    public Result<String> updateStatus(@RequestParam Integer id, @RequestParam Integer status) {
        try {
            String url = apiBaseUrl + "/category/" + id + "/status?status=" + status;
            restTemplate.put(url, null);
            return Result.success("状态更新成功");
        } catch (Exception e) {
            log.error("更新状态失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }
}
