package com.liang.shituadminweb.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 仪表盘页面控制器
 */
@Controller
@RequestMapping("/")
public class DashboardController {

    /**
     * 仪表盘页面
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // 页面标题
        model.addAttribute("pageTitle", "仪表盘");
        model.addAttribute("menu", "dashboard");

        // 这里只是返回视图名称，实际数据通过AJAX从 /api/dashboard/stats 加载
        return "dashboard";
    }

    /**
     * 根路径重定向到仪表盘
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }
}
