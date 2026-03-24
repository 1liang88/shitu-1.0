package com.liang.shitucommon;


import lombok.Data;

import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * 分页请求参数
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码，默认第1页
     */
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    /**
     * 每页数量，默认10条
     */
    @Min(value = 1, message = "每页数量最小为1")
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    private String orderBy;

    /**
     * 排序方式：asc/desc，默认desc
     */
    private String orderType = "desc";

    /**
     * 获取排序字符串
     */
    public String getOrderString() {
        if (orderBy == null || orderBy.trim().isEmpty()) {
            return null;
        }
        return orderBy + " " + orderType;
    }

    /**
     * 获取起始索引
     */
    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }

    /**
     * 获取限制数量
     */
    public int getLimit() {
        return pageSize;
    }
}