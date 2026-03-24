package com.liang.shituapi.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 点位查询参数
 */
@Data
public class PlaceQueryDTO {
    private String keyword;         // 关键词
    private Integer categoryId;     // 分类ID
    private BigDecimal longitude;    // 当前经度（用于附近查询）
    private BigDecimal latitude;     // 当前纬度（用于附近查询）
    private Double distance;         // 搜索距离（公里）
    private Integer limit;           // 返回数量限制
}
