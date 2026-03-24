package com.liang.shitucommon.entity;


import lombok.Data;
import java.time.LocalDateTime;

/**
 * 分类表实体
 */
@Data
public class Category {
    private Integer id;
    private String name;            // 分类名称
    private String code;            // 分类编码
    private String icon;            // 图标路径
    private Integer sort;           // 排序
    private String description;     // 描述
    private Integer status;         // 状态：0隐藏，1显示
    private LocalDateTime createTime;
}