package com.liang.shitucommon.entity;


import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 设施点位表实体
 */
@Data
public class Place {
    private Integer id;
    private String name;                // 点位名称
    private Integer categoryId;          // 分类ID
    private String address;              // 详细地址
    private BigDecimal longitude;        // 经度
    private BigDecimal latitude;         // 纬度
    private String images;               // 图片（多张用逗号分隔）
    private String contactPhone;         // 联系电话
    private String openingHours;         // 开放时间
    private String description;          // 描述/备注
    private String tags;                 // 标签
    private Integer visitCount;          // 访问次数
    private Integer favoriteCount;       // 收藏次数
    private BigDecimal rating;           // 评分
    private Integer status;              // 状态：0待审核，1已发布，2已下架
    private String createBy;             // 创建人
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 新增字段
    private Integer sourceType;  // 1-直接添加，2-上报审核
    private Integer sourceId;    // 来源ID（如果是上报，记录report_id）

    // 关联字段（非数据库字段）
    private String categoryName;         // 分类名称
}
