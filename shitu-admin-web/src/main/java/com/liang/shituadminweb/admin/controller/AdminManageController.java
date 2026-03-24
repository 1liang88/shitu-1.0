package com.liang.shituadminweb.admin.controller;

import com.liang.shitucommon.PageResult;
import com.liang.shitucommon.Result;
import com.liang.shitucommon.entity.Admin;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员管理控制器
 */
@Slf4j
@Controller
@RequestMapping("/admin")
public class AdminManageController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.base-url}")
    private String apiBaseUrl;

    /**
     * 管理员列表页面
     */
    @GetMapping
    public String list(@RequestParam(required = false) Integer pageNum,
                       @RequestParam(required = false) Integer pageSize,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer status,
                       Model model) {

        // 创建默认的空分页对象
        PageResult<Admin> emptyPage = new PageResult<>();
        emptyPage.setList(new ArrayList<>());
        emptyPage.setTotal(0L);
        emptyPage.setPages(0);
        emptyPage.setPageNum(pageNum != null ? pageNum : 1);
        emptyPage.setPageSize(pageSize != null ? pageSize : 10);

        try {
            // 构建URL
            StringBuilder url = new StringBuilder(apiBaseUrl + "/admin/page");
            url.append("?pageNum=").append(pageNum != null ? pageNum : 1)
                    .append("&pageSize=").append(pageSize != null ? pageSize : 10);
            if (keyword != null && !keyword.isEmpty()) {
                url.append("&keyword=").append(keyword);
            }
            if (status != null) {
                url.append("&status=").append(status);
            }

            log.info("调用管理员列表API: {}", url.toString());

            // 调用API获取管理员列表
            ParameterizedTypeReference<Result<PageResult<Admin>>> typeRef =
                    new ParameterizedTypeReference<Result<PageResult<Admin>>>() {};

            ResponseEntity<Result<PageResult<Admin>>> response = restTemplate.exchange(
                    url.toString(),
                    HttpMethod.GET,
                    null,
                    typeRef
            );

            if (response.getBody() != null && response.getBody().isSuccess()) {
                PageResult<Admin> pageData = response.getBody().getData();
                if (pageData != null) {
                    model.addAttribute("page", pageData);
                } else {
                    model.addAttribute("page", emptyPage);
                }
            } else {
                String errorMsg = response.getBody() != null ? response.getBody().getMessage() : "未知错误";
                log.warn("获取管理员列表失败: {}", errorMsg);
                model.addAttribute("page", emptyPage);
                model.addAttribute("error", errorMsg);
            }

        } catch (Exception e) {
            log.error("获取管理员列表异常", e);
            model.addAttribute("page", emptyPage);
            model.addAttribute("error", "获取管理员列表失败：" + e.getMessage());
        }

        // 保留查询条件
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("menu", "admin");
        model.addAttribute("title", "管理员管理");

        return "admin/list";
    }

    /**
     * 新增管理员页面
     */
    @GetMapping("/add")
    public String addPage(Model model) {
        model.addAttribute("admin", new Admin());
        model.addAttribute("menu", "admin");
        model.addAttribute("title", "新增管理员");
        return "admin/edit";
    }

    /**
     * 编辑管理员页面
     */
    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Integer id, Model model, HttpSession session) {
        try {
            // 检查是否是自己
            Integer currentId = (Integer) session.getAttribute("adminId");
            if (currentId != null && currentId.equals(id)) {
                model.addAttribute("isSelf", true);
            }

            String url = apiBaseUrl + "/admin/" + id;
            ParameterizedTypeReference<Result<Admin>> typeRef =
                    new ParameterizedTypeReference<Result<Admin>>() {};

            ResponseEntity<Result<Admin>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    typeRef
            );

            if (response.getBody() != null && response.getBody().isSuccess()) {
                Admin admin = response.getBody().getData();
                admin.setPassword(null); // 清除密码
                model.addAttribute("admin", admin);
            }

        } catch (Exception e) {
            log.error("获取管理员详情失败", e);
            model.addAttribute("error", "获取管理员详情失败：" + e.getMessage());
        }

        model.addAttribute("menu", "admin");
        model.addAttribute("title", "编辑管理员");

        return "admin/edit";
    }

    /**
     * 保存管理员（新增/修改）
     */
    @PostMapping("/save")
    public String save(Admin admin,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        try {
            String url = apiBaseUrl + "/admin";
            Map<String, Object> request = new HashMap<>();
            request.put("username", admin.getUsername());
            request.put("realName", admin.getRealName());
            request.put("role", admin.getRole() != null ? admin.getRole() : "admin");
            request.put("status", admin.getStatus() != null ? admin.getStatus() : 1);

            // 如果是新增，需要密码
            if (admin.getId() == null) {
                request.put("password", admin.getPassword() != null ? admin.getPassword() : "123456");
            }

            Result<?> response;
            if (admin.getId() != null) {
                // 修改
                ParameterizedTypeReference<Result<?>> typeRef =
                        new ParameterizedTypeReference<Result<?>>() {};
                ResponseEntity<Result<?>> responseEntity = restTemplate.exchange(
                        url + "/" + admin.getId(),
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
                return "redirect:/admin";
            } else {
                redirectAttributes.addFlashAttribute("error",
                        response != null ? response.getMessage() : "保存失败");
            }
        } catch (Exception e) {
            log.error("保存管理员失败", e);
            redirectAttributes.addFlashAttribute("error", "保存失败：" + e.getMessage());
        }

        return "redirect:/admin/edit" + (admin.getId() != null ? "/" + admin.getId() : "/add");
    }

    /**
     * 删除管理员
     */
    @PostMapping("/delete/{id}")
    @ResponseBody
    public Result<String> delete(@PathVariable Integer id, HttpSession session) {
        try {
            // 检查是否是自己
            Integer currentId = (Integer) session.getAttribute("adminId");
            if (currentId != null && currentId.equals(id)) {
                return Result.error("不能删除自己");
            }

            String url = apiBaseUrl + "/admin/" + id;
            restTemplate.delete(url);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除管理员失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 更新状态
     */
    @PostMapping("/update-status")
    @ResponseBody
    public Result<String> updateStatus(@RequestParam Integer id,
                                     @RequestParam Integer status,
                                     HttpSession session) {
        try {
            // 检查是否是自己
            Integer currentId = (Integer) session.getAttribute("adminId");
            if (currentId != null && currentId.equals(id) && status == 0) {
                return Result.error("不能禁用自己");
            }

            String url = apiBaseUrl + "/admin/" + id + "/status?status=" + status;
            restTemplate.put(url, null);
            return Result.success("状态更新成功");
        } catch (Exception e) {
            log.error("更新状态失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset-password/{id}")
    @ResponseBody
    public Result<String> resetPassword(@PathVariable Integer id) {
        try {
            String url = apiBaseUrl + "/admin/" + id + "/reset-password";
            restTemplate.put(url, null);
            return Result.success("密码已重置为：123456");
        } catch (Exception e) {
            log.error("重置密码失败", e);
            return Result.error("重置密码失败：" + e.getMessage());
        }
    }

    /**
     * 修改密码（个人）
     */
    @PostMapping("/change-password")
    @ResponseBody
    public Result<String> changePassword(@RequestParam String oldPassword,
                                       @RequestParam String newPassword,
                                       HttpSession session) {
        try {
            Integer id = (Integer) session.getAttribute("adminId");
            if (id == null) {
                return Result.unauthorized("请先登录");
            }

            String url = apiBaseUrl + "/admin/" + id + "/password?oldPassword=" + oldPassword +
                    "&newPassword=" + newPassword;
            restTemplate.put(url, null);
            return Result.success("密码修改成功");
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return Result.error("修改密码失败：" + e.getMessage());
        }
    }
}
