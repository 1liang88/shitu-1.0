package com.liang.shituadminweb.admin.controller;

import com.liang.shitucommon.PageResult;
import com.liang.shitucommon.Result;
import com.liang.shitucommon.entity.Category;
import com.liang.shitucommon.entity.Place;
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
import java.util.List;
import java.util.Map;

/**
 * 点位管理控制器
 */
@Slf4j
@Controller
@RequestMapping("/place")
public class PlaceController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.base-url}")
    private String apiBaseUrl;

    /**
     * 点位列表页面
     */
    @GetMapping
    public String list(@RequestParam(required = false) Integer pageNum,
                       @RequestParam(required = false) Integer pageSize,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer categoryId,
                       @RequestParam(required = false) Integer status,
                       Model model) {

        try {
            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("pageNum", pageNum != null ? pageNum : 1);
            params.put("pageSize", pageSize != null ? pageSize : 10);
            params.put("keyword", keyword);
            params.put("categoryId", categoryId);
            params.put("status", status);

            // 构建URL
            StringBuilder url = new StringBuilder(apiBaseUrl + "/place/page");
            url.append("?pageNum=").append(params.get("pageNum"))
                    .append("&pageSize=").append(params.get("pageSize"));
            if (keyword != null && !keyword.isEmpty()) {
                url.append("&keyword=").append(keyword);
            }
            if (categoryId != null) {
                url.append("&categoryId=").append(categoryId);
            }
            if (status != null) {
                url.append("&status=").append(status);
            }

            // 调用API获取点位列表
            ParameterizedTypeReference<Result<PageResult<Place>>> typeRef =
                    new ParameterizedTypeReference<Result<PageResult<Place>>>() {};

            ResponseEntity<Result<PageResult<Place>>> response = restTemplate.exchange(
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
            log.error("获取点位列表失败", e);
            model.addAttribute("error", "获取点位列表失败：" + e.getMessage());
        }

        // 保留查询条件
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("status", status);
        model.addAttribute("menu", "place");
        model.addAttribute("title", "点位管理");

        return "place/list";
    }

    /**
     * 新增点位页面
     */
    @GetMapping("/add")
    public String addPage(Model model) {
        // 获取分类列表
        try {
            String categoryUrl = apiBaseUrl + "/category/list";
            ParameterizedTypeReference<Result<List<Category>>> typeRef =
                    new ParameterizedTypeReference<Result<List<Category>>>() {};

            ResponseEntity<Result<List<Category>>> response = restTemplate.exchange(
                    categoryUrl,
                    HttpMethod.GET,
                    null,
                    typeRef
            );

            if (response.getBody() != null && response.getBody().isSuccess()) {
                model.addAttribute("categories", response.getBody().getData());
            }
        } catch (Exception e) {
            log.error("获取分类列表失败", e);
        }

        model.addAttribute("place", new Place());
        model.addAttribute("menu", "place");
        model.addAttribute("title", "新增点位");

        return "place/edit";
    }

    /**
     * 编辑点位页面
     */
    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Integer id, Model model) {
        try {
            // 获取点位详情
            String placeUrl = apiBaseUrl + "/place/" + id;
            ParameterizedTypeReference<Result<Place>> placeTypeRef =
                    new ParameterizedTypeReference<Result<Place>>() {};

            ResponseEntity<Result<Place>> placeResponse = restTemplate.exchange(
                    placeUrl,
                    HttpMethod.GET,
                    null,
                    placeTypeRef
            );

            if (placeResponse.getBody() != null && placeResponse.getBody().isSuccess()) {
                model.addAttribute("place", placeResponse.getBody().getData());
            }

            // 获取分类列表
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
            log.error("获取点位详情失败", e);
            model.addAttribute("error", "获取点位详情失败：" + e.getMessage());
        }

        model.addAttribute("menu", "place");
        model.addAttribute("title", "编辑点位");

        return "place/edit";
    }

    /**
     * 查看点位详情
     */
    @GetMapping("/view/{id}")
    public String viewPage(@PathVariable Integer id, Model model) {
        try {
            String url = apiBaseUrl + "/place/" + id;
            ParameterizedTypeReference<Result<Place>> typeRef =
                    new ParameterizedTypeReference<Result<Place>>() {};

            ResponseEntity<Result<Place>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    typeRef
            );

            if (response.getBody() != null && response.getBody().isSuccess()) {
                model.addAttribute("place", response.getBody().getData());
            }
        } catch (Exception e) {
            log.error("获取点位详情失败", e);
            model.addAttribute("error", "获取点位详情失败：" + e.getMessage());
        }

        model.addAttribute("menu", "place");
        model.addAttribute("title", "点位详情");

        return "place/view";
    }

    /**
     * 保存点位（新增/修改）
     */
    @PostMapping("/save")
    public String save(Place place,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        try {
            String url = apiBaseUrl + "/place";
            Map<String, Object> request = new HashMap<>();
            request.put("name", place.getName());
            request.put("categoryId", place.getCategoryId());
            request.put("address", place.getAddress());
            request.put("longitude", place.getLongitude());
            request.put("latitude", place.getLatitude());
            request.put("contactPhone", place.getContactPhone());
            request.put("openingHours", place.getOpeningHours());
            request.put("description", place.getDescription());
            request.put("tags", place.getTags());
            request.put("status", place.getStatus() != null ? place.getStatus() : 1);

            // 获取当前管理员
            String adminName = (String) session.getAttribute("adminName");
            request.put("createBy", adminName != null ? adminName : "系统");

            Result<?> response;
            if (place.getId() != null) {
                // 修改
                ParameterizedTypeReference<Result<?>> typeRef =
                        new ParameterizedTypeReference<Result<?>>() {};
                ResponseEntity<Result<?>> responseEntity = restTemplate.exchange(
                        url + "/" + place.getId(),
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
                return "redirect:/place";
            } else {
                redirectAttributes.addFlashAttribute("error",
                        response != null ? response.getMessage() : "保存失败");
            }
        } catch (Exception e) {
            log.error("保存点位失败", e);
            redirectAttributes.addFlashAttribute("error", "保存失败：" + e.getMessage());
        }

        return "redirect:/place/edit" + (place.getId() != null ? "/" + place.getId() : "/add");
    }

    /**
     * 删除点位
     */
    @PostMapping("/delete/{id}")
    @ResponseBody
    public Result<String> delete(@PathVariable Integer id) {
        try {
            String url = apiBaseUrl + "/place/" + id;
            restTemplate.delete(url);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除点位失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除
     */
    @PostMapping("/batch-delete")
    @ResponseBody
    public Result<String> batchDelete(@RequestBody List<Integer> ids) {
        try {
            String url = apiBaseUrl + "/place/batch";
            restTemplate.postForObject(url, ids, Result.class);
            return Result.success("批量删除成功");
        } catch (Exception e) {
            log.error("批量删除失败", e);
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }

    /**
     * 更新状态
     */
    @PostMapping("/update-status")
    @ResponseBody
    public Result<String> updateStatus(@RequestParam Integer id, @RequestParam Integer status) {
        try {
            String url = apiBaseUrl + "/place/" + id + "/status?status=" + status;
            restTemplate.put(url, null);
            return Result.success("状态更新成功");
        } catch (Exception e) {
            log.error("更新状态失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 批量更新状态
     */
    @PostMapping("/batch-update-status")
    @ResponseBody
    public Result<String> batchUpdateStatus(@RequestBody List<Integer> ids, @RequestParam Integer status) {
        try {
            String url = apiBaseUrl + "/place/batch/status?status=" + status;
            restTemplate.put(url, ids);
            return Result.success("批量状态更新成功");
        } catch (Exception e) {
            log.error("批量更新状态失败", e);
            return Result.error("批量更新失败：" + e.getMessage());
        }
    }
}

