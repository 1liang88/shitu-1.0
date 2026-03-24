package com.liang.shitucommon.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 管理员表实体
 */
@Data
public class Admin {
    private Integer id;
    private String username;        // 用户名
    private String password;        // 密码（加密存储）
    private String realName;        // 真实姓名
    private String avatar;          // 头像
    private String role;            // 角色
    private LocalDateTime lastLoginTime;  // 最后登录时间
    private String lastLoginIp;      // 最后登录IP
    private Integer status;          // 状态：0禁用，1启用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
