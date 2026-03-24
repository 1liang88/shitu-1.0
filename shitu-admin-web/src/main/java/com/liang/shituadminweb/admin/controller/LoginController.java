package com.liang.shituadminweb.admin.controller;

import com.liang.shitucommon.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录控制器
 */
@Slf4j
@Controller
public class LoginController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.base-url}")
    private String apiBaseUrl;

    /**
     * 登录页面
     */
    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        // 如果已经登录，直接跳转到仪表盘
        if (session.getAttribute("adminId") != null) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    /**
     * 登录处理 - 接收表单提交，向API发送JSON
     */
    @PostMapping("/doLogin")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        try {
            // 1. 接收表单提交的用户名密码

            // 2. 组装JSON请求体
            Map<String, String> request = new HashMap<>();
            request.put("username", username);
            request.put("password", password);

            // 3. 向API发送JSON请求，使用Result类型接收
            String url = apiBaseUrl + "/admin/login";
            log.info("调用登录API: {}, 用户名: {}", url, username);

            // 使用 ParameterizedTypeReference 保留泛型信息
            ParameterizedTypeReference<Result<Map<String, Object>>> typeRef =
                    new ParameterizedTypeReference<Result<Map<String, Object>>>() {};

            ResponseEntity<Result<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    typeRef
            );

            // 4. 处理API返回的JSON响应
            if (response.getBody() != null && response.getBody().isSuccess()) {
                Result<Map<String, Object>> result = response.getBody();
                Map<String, Object> data = result.getData();  // 从data字段获取
                Map<String, Object> admin = (Map<String, Object>) data.get("admin");

                // 保存登录信息到session
                session.setAttribute("adminId", admin.get("id"));
                session.setAttribute("adminName", admin.get("realName"));
                session.setAttribute("adminUsername", admin.get("username"));
                session.setAttribute("role", admin.get("role"));

                log.info("管理员登录成功：{}", username);

                // 登录成功，重定向到仪表盘
                return "redirect:/dashboard";
            } else {
                // 登录失败，显示错误信息
                String errorMsg = response.getBody() != null ?
                        response.getBody().getMessage() : "登录失败";
                redirectAttributes.addFlashAttribute("error", errorMsg);
                log.warn("登录失败：{}", errorMsg);
            }
        } catch (Exception e) {
            log.error("登录异常", e);
            redirectAttributes.addFlashAttribute("error", "系统异常，请稍后重试");
        }

        // 登录失败，返回登录页
        return "redirect:/login";
    }

    /**
     * 退出登录
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}