package com.liang.shituapi.controller;

import com.liang.shitucommon.Result;
import com.liang.shituapi.service.PlaceService;
import com.liang.shituapi.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘控制器
 */
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    @Autowired
    private PlaceService placeService;

    @Autowired
    private ReportService reportService;

    /**
     * 获取统计数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // 点位统计
        List<Map<String, Object>> categoryStats = placeService.countByCategory();
        stats.put("placeByCategory", categoryStats);

        // 上报统计
        stats.put("reportPending", reportService.getPendingCount());
        stats.put("reportByType", reportService.countByType());

        // 总数统计
        long totalPlaces = placeService.getAll().size();
        long totalReports = reportService.getAll().size();

        stats.put("totalPlaces", totalPlaces);
        stats.put("totalReports", totalReports);

        return Result.success(stats);
    }

    /**
     * 获取热门区域
     */
    @GetMapping("/hot-areas")
    public Object getHotAreas() {
        List<Map<String, Object>> hotAreas = placeService.countByHotArea(10);
        return Result.success(hotAreas);
    }
}
