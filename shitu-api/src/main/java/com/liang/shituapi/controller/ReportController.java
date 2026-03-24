package com.liang.shituapi.controller;

import com.liang.shituapi.service.ReportService;
import com.liang.shitucommon.Constants;
import com.liang.shitucommon.PageRequest;
import com.liang.shitucommon.PageResult;
import com.liang.shitucommon.Result;
import com.liang.shitucommon.entity.Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 上报控制器
 */
@RestController
@RequestMapping("/api/v1/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 分页查询上报列表（管理端）
     */
    @GetMapping("/page")
    public Result<PageResult<Report>> page(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer reportType,
            @RequestParam(required = false) Integer status) {
        // 分页参数合法性校验
        pageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        pageSize = pageSize == null || pageSize < 1 || pageSize > 100 ? 10 : pageSize;

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(pageNum);
        pageRequest.setPageSize(pageSize);

        PageResult<Report> pageResult = reportService.page(pageRequest, keyword, categoryId, reportType, status);
        return Result.success(pageResult);
    }

    /**
     * 查询待处理数量
     */
    @GetMapping("/pending/count")
    public Result<Long> getPendingCount() {
        Long count = reportService.getPendingCount();
        return Result.success(count);
    }

    /**
     * 根据ID查询上报详情
     */
    @GetMapping("/{id}")
    public Result<Report> getById(@PathVariable Integer id) {
        // 校验ID非空
        if (id == null || id < 1) {
            return Result.error("上报ID不能为空且必须为正整数");
        }

        Report report = reportService.getById(id);
        if (report == null) {
            return Result.notFound("上报记录不存在");
        }
        return Result.success(report);
    }

    /**
     * 新增上报（用户端）
     */
    @PostMapping
    public Result<String> add(@RequestBody Report report) {
        // 校验上报对象非空
        if (report == null) {
            return Result.error("上报信息不能为空");
        }

        boolean success = reportService.add(report);
        return success ? Result.success("上报成功，感谢您的贡献") : Result.error("上报失败");
    }

    /**
     * 修改上报（仅限未审核的）
     */
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Integer id, @RequestBody Report report) {
        // 基础校验
        if (id == null || id < 1 || report == null) {
            return Result.error("参数不合法：ID不能为空且为正整数，上报信息不能为空");
        }

        report.setId(id);
        boolean success = reportService.update(report);
        return success ? Result.success("修改成功") : Result.error("修改失败");
    }

    /**
     * 删除上报（仅限未审核的）
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        if (id == null || id < 1) {
            return Result.error("上报ID不能为空且必须为正整数");
        }

        boolean success = reportService.deleteById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 批量删除上报
     */
    @DeleteMapping("/batch")
    public Result<String> batchDelete(@RequestBody List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error("批量删除的ID列表不能为空");
        }

        boolean success = reportService.deleteBatch(ids);
        return success ? Result.success("批量删除成功") : Result.error("批量删除失败");
    }

    /**
     * 审核上报
     */
    @PutMapping("/{id}/audit")
    public Result<String> audit(
            @PathVariable Integer id,
            @RequestParam Integer status,
            @RequestParam(required = false) String remark,
            @RequestParam(required = false) Integer adminId, // 新增：接收转发的adminId
            HttpServletRequest request) {
        // 1. 参数校验
        if (id == null || id < 1) {
            return Result.error("上报ID不能为空且必须为正整数");
        }
        if (status == null || !List.of(Constants.ReportStatus.PENDING, Constants.ReportStatus.APPROVED, Constants.ReportStatus.REJECTED).contains(status)) {
            return Result.error("审核状态不合法，仅支持：待审核/通过/驳回");
        }

        // 2. 获取当前管理员ID（优先取请求参数，兼容Session方式）
        Integer finalAdminId = adminId;
        if (finalAdminId == null) {
            finalAdminId = (Integer) request.getSession().getAttribute("adminId");
        }
        if (finalAdminId == null) {
            return Result.unauthorized("请先登录");
        }

        // 3. 执行审核
        boolean success = reportService.audit(id, status, remark, finalAdminId);
        String message = status == Constants.ReportStatus.APPROVED ? "审核通过" : "已驳回";
        return success ? Result.success(message) : Result.error("审核失败");
    }
    /**
     * 批量审核
     */
    @PutMapping("/batch/audit")
    public Result<String> batchAudit(
            @RequestBody List<Integer> ids,
            @RequestParam Integer status,
            @RequestParam(required = false) String remark,
            HttpServletRequest request) {
        // 参数校验
        if (ids == null || ids.isEmpty()) {
            return Result.error("批量审核的ID列表不能为空");
        }
        if (status == null || !List.of(Constants.ReportStatus.APPROVED, Constants.ReportStatus.REJECTED).contains(status)) {
            return Result.error("审核状态不合法，仅支持：通过/驳回");
        }

        // 获取管理员ID
        Integer adminId = (Integer) request.getSession().getAttribute("adminId");
        if (adminId == null) {
            return Result.unauthorized("请先登录");
        }

        boolean success = reportService.batchAudit(ids, status, remark, adminId);
        String message = status == Constants.ReportStatus.APPROVED ? "批量审核通过" : "批量驳回";
        return success ? Result.success(message) : Result.error("批量审核失败");
    }

    /**
     * 批量通过
     */
    @PutMapping("/batch/approve")
    public Result<String> batchApprove(
            @RequestBody List<Integer> ids,
            @RequestParam(required = false) String remark,
            HttpServletRequest request) {
        // 复用批量审核逻辑，指定状态为通过
        return batchAudit(ids, Constants.ReportStatus.APPROVED, remark, request);
    }

    /**
     * 批量驳回
     */
    @PutMapping("/batch/reject")
    public Result<String> batchReject(
            @RequestBody List<Integer> ids,
            @RequestParam(required = false) String remark,
            HttpServletRequest request) {
        // 复用批量审核逻辑，指定状态为驳回
        return batchAudit(ids, Constants.ReportStatus.REJECTED, remark, request);
    }

    /**
     * 按类型统计
     */
    @GetMapping("/stats/type")
    public Result<List<Map<String, Object>>> countByType() {
        List<Map<String, Object>> stats = reportService.countByType();
        return Result.success(stats);
    }

    /**
     * 按状态统计
     */
    @GetMapping("/stats/status")
    public Result<Map<String, Object>> countByStatus() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("pending", reportService.countByStatus(Constants.ReportStatus.PENDING));
        stats.put("approved", reportService.countByStatus(Constants.ReportStatus.APPROVED));
        stats.put("rejected", reportService.countByStatus(Constants.ReportStatus.REJECTED));
        return Result.success(stats);
    }

    /**
     * 获取统计概览
     */
    @GetMapping("/stats/overview")
    public Result<Map<String, Object>> getStatistics() {
        // 【优化建议】：在ReportService接口中新增getStatistics()方法，避免强转实现类
        Map<String, Object> stats = reportService.getStatistics();
        return Result.success(stats);
    }

    /**
     * 查询用户自己的上报记录（根据手机号）
     */
    @GetMapping("/user/{phone}")
    public Result<List<Report>> getUserReports(@PathVariable String phone) {
        // 校验手机号非空
        if (phone == null || phone.trim().isEmpty()) {
            return Result.error("手机号不能为空");
        }

        // 【优化建议】：在ReportService接口中新增getUserReports(String phone)方法
        List<Report> list = reportService.getUserReports(phone);
        return Result.success(list);
    }
}