package com.liang.shitucommon.enums;

import lombok.Getter;

/**
 * 上报状态枚举
 */
@Getter
public enum ReportStatusEnum {
    PENDING(0, "待处理"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已驳回");

    private final Integer code;
    private final String description;

    ReportStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
}