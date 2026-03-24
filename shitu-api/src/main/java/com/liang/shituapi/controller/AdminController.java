package com.liang.shituapi.controller;

import com.liang.shitucommon.Constants;
import com.liang.shitucommon.PageRequest;
import com.liang.shitucommon.PageResult;
import com.liang.shitucommon.Result;
import com.liang.shituapi.dto.LoginDTO;
import com.liang.shitucommon.entity.Admin;
import com.liang.shituapi.service.AdminService;
import com.liang.shitucommon.entity.Category;
import com.liang.shitucommon.entity.Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Validated @RequestBody LoginDTO loginDTO,
                                             HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        Admin admin = adminService.login(loginDTO.getUsername(), loginDTO.getPassword(), ip);

        // 保存到session
        HttpSession session = request.getSession();
        session.setAttribute("adminId", admin.getId());
        session.setAttribute("adminName", admin.getRealName());

        Map<String, Object> result = new HashMap<>();
        result.put("admin", admin);
        result.put("sessionId", session.getId());

        return Result.success("登录成功", result);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return Result.success("退出成功");
    }

    /**
     * 获取当前登录管理员信息
     */
    @GetMapping("/current")
    public Object getCurrentAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            return Result.unauthorized("未登录");
        }

        Integer adminId = (Integer) session.getAttribute("adminId");
        Admin admin = adminService.getById(adminId);
        admin.setPassword(null); // 清除密码

        return Result.success(admin);
    }

    /**
     * 分页查询管理员列表
     */
    @GetMapping("/page")
    public Object page(@RequestParam(required = false) Integer pageNum,
                                          @RequestParam(required = false) Integer pageSize,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) Integer status) {
        PageRequest pageRequest = new PageRequest();
        if (pageNum != null) pageRequest.setPageNum(pageNum);
        if (pageSize != null) pageRequest.setPageSize(pageSize);

        PageResult<Admin> pageResult = adminService.page(pageRequest, keyword, status);

        // 清除密码
        if (pageResult.getList() != null) {
            pageResult.getList().forEach(admin -> admin.setPassword(null));
        }

        return Result.success(pageResult);
    }

    /**
     * 查询所有管理员
     */
    @GetMapping("/list")
    public Object list() {
        List<Admin> list = adminService.getAll();
        list.forEach(admin -> admin.setPassword(null));
        return Result.success(list);
    }

    /**
     * 根据ID查询管理员
     */
    @GetMapping("/{id}")
    public Object getById(@PathVariable Integer id) {
        Admin admin = adminService.getById(id);
        if (admin == null) {
            return Result.notFound("管理员不存在");
        }
        admin.setPassword(null);
        return Result.success(admin);
    }

    /**
     * 新增管理员
     */
    @PostMapping
    public Result<String> add(@Validated @RequestBody Admin admin) {
        // 设置默认值
        if (admin.getRole() == null) {
            admin.setRole(Constants.Role.ADMIN);
        }
        if (admin.getStatus() == null) {
            admin.setStatus(Constants.Status.ENABLED);
        }

        boolean success = adminService.add(admin);
        return success ? Result.success("新增成功") : Result.error("新增失败");
    }

    /**
     * 修改管理员
     */
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Integer id, @RequestBody Admin admin) {
        admin.setId(id);
        boolean success = adminService.update(admin);
        return success ? Result.success("修改成功") : Result.error("修改失败");
    }

    /**
     * 删除管理员
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        boolean success = adminService.deleteById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 批量删除管理员
     */
    @DeleteMapping("/batch")
    public Result<String> batchDelete(@RequestBody List<Integer> ids) {
        boolean success = adminService.deleteBatch(ids);
        return success ? Result.success("批量删除成功") : Result.error("批量删除失败");
    }

    /**
     * 修改密码
     */
    @PutMapping("/{id}/password")
    public Result<String> changePassword(@PathVariable Integer id,
                                       @RequestParam String oldPassword,
                                       @RequestParam String newPassword) {
        boolean success = adminService.changePassword(id, oldPassword, newPassword);
        return success ? Result.success("密码修改成功") : Result.error("密码修改失败");
    }

    /**
     * 重置密码
     */
    @PutMapping("/{id}/reset-password")
    public Result<String> resetPassword(@PathVariable Integer id) {
        boolean success = adminService.resetPassword(id);
        return success ? Result.success("密码重置成功（新密码：123456）") : Result.error("密码重置失败");
    }

    /**
     * 更新状态
     */
    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable Integer id,
                                     @RequestParam Integer status) {
        boolean success = adminService.updateStatus(id, status);
        return success ? Result.success("状态更新成功") : Result.error("状态更新失败");
    }
}