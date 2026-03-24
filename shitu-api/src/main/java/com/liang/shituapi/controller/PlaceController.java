package com.liang.shituapi.controller;

import com.liang.shitucommon.Constants;
import com.liang.shitucommon.PageRequest;
import com.liang.shitucommon.PageResult;
import com.liang.shitucommon.Result;
import com.liang.shituapi.dto.PlaceQueryDTO;
import com.liang.shitucommon.entity.Admin;
import com.liang.shitucommon.entity.Place;
import com.liang.shituapi.service.PlaceService;
import com.liang.shitucommon.entity.Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 设施点位控制器
 */
@RestController
@RequestMapping("/api/v1/place")
public class PlaceController {

    @Autowired
    private PlaceService placeService;

    /**
     * 分页查询点位列表（管理端）
     */
    @GetMapping("/page")
    public Object page(@RequestParam(required = false) Integer pageNum,
                                          @RequestParam(required = false) Integer pageSize,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) Integer categoryId,
                                          @RequestParam(required = false) Integer status) {
        PageRequest pageRequest = new PageRequest();
        if (pageNum != null) pageRequest.setPageNum(pageNum);
        if (pageSize != null) pageRequest.setPageSize(pageSize);

        PageResult<Place> pageResult = placeService.page(pageRequest, keyword, categoryId, status);
        return Result.success(pageResult);
    }

    /**
     * 查询所有已发布的点位（用户端）
     */
    @GetMapping("/list")
    public Object list(@RequestParam(required = false) Integer categoryId) {
        List<Place> list = placeService.getByCategoryId(categoryId, Constants.Status.ENABLED);
        return Result.success(list);
    }

    /**
     * 查询附近点位（用户端）
     */
    @GetMapping("/nearby")
    public Object nearby(PlaceQueryDTO queryDTO) {
        if (queryDTO.getLongitude() == null || queryDTO.getLatitude() == null) {
            return Result.badRequest("经纬度不能为空");
        }

        List<Place> list = placeService.findNearby(
                queryDTO.getLongitude(),
                queryDTO.getLatitude(),
                queryDTO.getDistance(),
                queryDTO.getCategoryId(),
                queryDTO.getLimit()
        );
        return Result.success(list);
    }

    /**
     * 根据ID查询点位详情
     */
    @GetMapping("/{id}")
    public Object getById(@PathVariable Integer id, HttpServletRequest request) {
        Place place = placeService.getById(id);
        if (place == null) {
            return Result.notFound("点位不存在");
        }

        // 检查是否需要隐藏未发布的点位（非管理员）
        String role = (String) request.getSession().getAttribute("role");
        if (place.getStatus() != Constants.Status.ENABLED && !"admin".equals(role)) {
            return Result.notFound("点位不存在");
        }

        return Result.success(place);
    }

    /**
     * 新增点位
     */
    @PostMapping
    public Result<String> add(@RequestBody Place place, HttpServletRequest request) {
        // 设置创建人
        String adminName = (String) request.getSession().getAttribute("adminName");
        place.setCreateBy(adminName != null ? adminName : "系统");

        // 设置默认状态
        if (place.getStatus() == null) {
            place.setStatus(Constants.Status.ENABLED);
        }

        boolean success = placeService.add(place);
        return success ? Result.success("新增成功") : Result.error("新增失败");
    }

    /**
     * 修改点位
     */
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Integer id, @RequestBody Place place) {
        place.setId(id);
        boolean success = placeService.update(place);
        return success ? Result.success("修改成功") : Result.error("修改失败");
    }

    /**
     * 删除点位
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        boolean success = placeService.deleteById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 批量删除点位
     */
    @DeleteMapping("/batch")
    public Result<String> batchDelete(@RequestBody List<Integer> ids) {
        boolean success = placeService.deleteBatch(ids);
        return success ? Result.success("批量删除成功") : Result.error("批量删除失败");
    }

    /**
     * 更新状态
     */
    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable Integer id,
                                     @RequestParam Integer status) {
        boolean success = placeService.updateStatus(id, status);
        return success ? Result.success("状态更新成功") : Result.error("状态更新失败");
    }

    /**
     * 批量更新状态
     */
    @PutMapping("/batch/status")
    public Result<String> batchUpdateStatus(@RequestBody List<Integer> ids,
                                          @RequestParam Integer status) {
        boolean success = placeService.batchUpdateStatus(ids, status);
        return success ? Result.success("批量状态更新成功") : Result.error("批量状态更新失败");
    }

    /**
     * 分类统计
     */
    @GetMapping("/stats/category")
    public Object countByCategory() {
        List<Map<String, Object>> stats = placeService.countByCategory();
        return Result.success(stats);
    }

    /**
     * 热门区域统计
     */
    @GetMapping("/stats/hot-area")
    public Object countByHotArea(@RequestParam(required = false) Integer limit) {
        if (limit == null) limit = 10;
        List<Map<String, Object>> stats = placeService.countByHotArea(limit);
        return Result.success(stats);
    }
}
