package com.liang.shitucommon;


/**
 * 系统常量类
 */
public interface Constants {

    /**
     * 状态码
     */
    interface Code {
        int SUCCESS = 200;
        int BAD_REQUEST = 400;
        int UNAUTHORIZED = 401;
        int FORBIDDEN = 403;
        int NOT_FOUND = 404;
        int ERROR = 500;
    }

    /**
     * 状态信息
     */
    interface Message {
        String SUCCESS = "操作成功";
        String ERROR = "操作失败";
        String BAD_REQUEST = "请求参数错误";
        String UNAUTHORIZED = "未登录或登录已过期";
        String FORBIDDEN = "没有权限访问";
        String NOT_FOUND = "资源不存在";
    }

    /**
     * 通用状态
     */
    interface Status {
        int DISABLED = 0;  // 禁用/隐藏/待审核
        int ENABLED = 1;   // 启用/显示/已发布
        int REJECTED = 2;  // 已驳回/已下架
    }

    /**
     * 上报类型
     */
    interface ReportType {
        int ADD = 1;       // 新增
        int CORRECT = 2;   // 纠错
        int FEEDBACK = 3;  // 反馈
    }

    /**
     * 上报状态
     */
    interface ReportStatus {
        int PENDING = 0;   // 待处理
        int APPROVED = 1;  // 已通过
        int REJECTED = 2;  // 已驳回
    }

    /**
     * 管理员角色
     */
    interface Role {
        String SUPER_ADMIN = "super_admin";  // 超级管理员
        String ADMIN = "admin";              // 普通管理员
    }

    /**
     * 默认值
     */
    interface Default {
        int PAGE_NUM = 1;
        int PAGE_SIZE = 10;
        String PASSWORD = "123456";
    }
}
