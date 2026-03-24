package com.liang.shitucommon.enums;

import lombok.Getter;

/**
 * 上报类型枚举
 */
@Getter
public enum ReportTypeEnum {
    ADD(1, "新增"),
    CORRECT(2, "纠错"),
    FEEDBACK(3, "反馈");

    private final Integer code;
    private final String description;

    ReportTypeEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
}
