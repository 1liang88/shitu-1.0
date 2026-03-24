package com.liang.shitucommon;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页结果类
 * @param <T> 数据类型
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Integer pages;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页数量
     */
    private Integer pageSize;

    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;

    /**
     * 空构造方法
     */
    public PageResult() {
    }

    /**
     * 全参构造方法
     */
    public PageResult(Long total, Integer pages, Integer pageNum, Integer pageSize, List<T> list) {
        this.total = total;
        this.pages = pages;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.list = list;
        this.hasNext = pageNum < pages;
        this.hasPrevious = pageNum > 1;
    }

    /**
     * 创建空分页结果
     */
    public static <T> PageResult<T> empty() {
        PageResult<T> result = new PageResult<>();
        result.setTotal(0L);
        result.setPages(0);
        result.setPageNum(1);
        result.setPageSize(10);
        result.setList(Collections.emptyList());
        result.setHasNext(false);
        result.setHasPrevious(false);
        return result;
    }

    /**
     * 创建单页分页结果
     */
    public static <T> PageResult<T> single(List<T> list) {
        PageResult<T> result = new PageResult<>();
        result.setTotal((long) list.size());
        result.setPages(1);
        result.setPageNum(1);
        result.setPageSize(list.size());
        result.setList(list);
        result.setHasNext(false);
        result.setHasPrevious(false);
        return result;
    }

    /**
     * 从MyBatis分页结果创建 - 移除对PageHelper的直接依赖
     */
    public static <T> PageResult<T> of(List<T> list, long total, int pageNum, int pageSize) {
        int pages = (int) (total + pageSize - 1) / pageSize;
        return new PageResult<>(total, pages, pageNum, pageSize, list);
    }

    @Override
    public String toString() {
        return "PageResult{" +
                "total=" + total +
                ", pages=" + pages +
                ", pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", list.size=" + (list != null ? list.size() : 0) +
                ", hasNext=" + hasNext +
                ", hasPrevious=" + hasPrevious +
                '}';
    }
}