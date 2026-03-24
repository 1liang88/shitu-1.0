package com.liang.shitucommon.enums;

import lombok.Getter;

/**
 * 通用状态枚举
 */
@Getter
public enum StatusEnum {
    DISABLED(0, "禁用/隐藏/待审核"),
    ENABLED(1, "启用/显示/已发布"),
    REJECTED(2, "已驳回/已下架");

    private final Integer code;
    private final String description;

    StatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String getDescriptionByCode(Integer code) {
        if (code == null) return "";
        for (StatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value.description;
            }
        }
        return "";
    }
}
