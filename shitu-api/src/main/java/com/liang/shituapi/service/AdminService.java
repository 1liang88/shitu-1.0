package com.liang.shituapi.service;

import com.liang.shitucommon.entity.Admin;
import com.liang.shitucommon.PageRequest;
import com.liang.shitucommon.PageResult;

public interface AdminService extends BaseService<Admin, Integer> {
    Admin login(String username, String password, String ip);
    Admin getByUsername(String username);
    PageResult<Admin> page(PageRequest pageRequest, String keyword, Integer status);
    boolean changePassword(Integer id, String oldPassword, String newPassword);
    boolean resetPassword(Integer id);
    boolean updateStatus(Integer id, Integer status);
    boolean checkUsernameExists(String username, Integer excludeId);
}