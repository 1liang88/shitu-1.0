package com.liang.shituapi.controller;

import com.liang.shitucommon.Constants;
import com.liang.shitucommon.PageRequest;
import com.liang.shitucommon.PageResult;
import com.liang.shitucommon.Result;
import com.liang.shitucommon.entity.Admin;
import com.liang.shitucommon.entity.Category;
import com.liang.shituapi.service.CategoryService;
import com.liang.shitucommon.entity.Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器
 */
@RestController
@RequestMapping("/api/v1/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 分页查询分类列表
     */
    @GetMapping("/page")
    public Object page(@RequestParam(required = false) Integer pageNum,
                                             @RequestParam(required = false) Integer pageSize,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) Integer status) {
        PageRequest pageRequest = new PageRequest();
        if (pageNum != null) pageRequest.setPageNum(pageNum);
        if (pageSize != null) pageRequest.setPageSize(pageSize);

        PageResult<Category> pageResult = categoryService.page(pageRequest, keyword, status);
        return Result.success(pageResult);
    }

    /**
     * 查询所有分类
     */
    @GetMapping("/list")
    public Object list() {
        List<Category> list = categoryService.getAll();
        return Result.success(list);
    }

    /**
     * 查询所有启用的分类（供用户端使用）
     */
    @GetMapping("/enabled")
    public Object getEnabled() {
        List<Category> list = categoryService.getEnabled();
        return Result.success(list);
    }

    /**
     * 根据ID查询分类
     */
    @GetMapping("/{id}")
    public Object getById(@PathVariable Integer id) {
        Category category = categoryService.getById(id);
        if (category == null) {
            return Result.notFound("分类不存在");
        }
        return Result.success(category);
    }

    /**
     * 根据编码查询分类
     */
    @GetMapping("/code/{code}")
    public Object getByCode(@PathVariable String code) {
        Category category = categoryService.getByCode(code);
        if (category == null) {
            return Result.notFound("分类不存在");
        }
        return Result.success(category);
    }

    /**
     * 新增分类
     */
    @PostMapping
    public Result<String> add(@RequestBody Category category) {
        // 设置默认值
        if (category.getStatus() == null) {
            category.setStatus(Constants.Status.ENABLED);
        }

        boolean success = categoryService.add(category);
        return success ? Result.success("新增成功") : Result.error("新增失败");
    }

    /**
     * 修改分类
     */
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Integer id, @RequestBody Category category) {
        category.setId(id);
        boolean success = categoryService.update(category);
        return success ? Result.success("修改成功") : Result.error("修改失败");
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        boolean success = categoryService.deleteById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 批量删除分类
     */
    @DeleteMapping("/batch")
    public Result<String> batchDelete(@RequestBody List<Integer> ids) {
        boolean success = categoryService.deleteBatch(ids);
        return success ? Result.success("批量删除成功") : Result.error("批量删除失败");
    }

    /**
     * 更新状态
     */
    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable Integer id,
                                     @RequestParam Integer status) {
        boolean success = categoryService.updateStatus(id, status);
        return success ? Result.success("状态更新成功") : Result.error("状态更新失败");
    }

    /**
     * 更新排序
     */
    @PutMapping("/{id}/sort")
    public Result<String> updateSort(@PathVariable Integer id,
                                   @RequestParam Integer sort) {
        boolean success = categoryService.updateSort(id, sort);
        return success ? Result.success("排序更新成功") : Result.error("排序更新失败");
    }
}
