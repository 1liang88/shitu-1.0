package com.liang.shitucommon.entity;


import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户上报/审核表实体
 */
@Data
public class Report {
    private Integer id;
    private String placeName;           // 点位名称
    private Integer categoryId;          // 分类ID
    private String address;              // 地址
    private BigDecimal longitude;        // 经度
    private BigDecimal latitude;         // 纬度
    private String contactInfo;          // 联系方式
    private Integer reportType;          // 类型：1新增，2纠错，3反馈
    private String content;              // 上报内容
    private String images;               // 图片
    private String reporterName;         // 上报人姓名
    private String reporterPhone;        // 上报人电话
    private Integer status;              // 状态：0待处理，1已通过，2已驳回
    private String auditRemark;          // 审核备注
    private LocalDateTime auditTime;      // 审核时间
    private Integer auditBy;              // 审核人ID
    private LocalDateTime createTime;

    // 关联字段
    private String categoryName;         // 分类名称
    private String auditByName;          // 审核人姓名
}
