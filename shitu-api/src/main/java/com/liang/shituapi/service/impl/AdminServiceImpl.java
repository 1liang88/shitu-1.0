package com.liang.shituapi.service.impl;

import com.liang.shituapi.dao.AdminMapper;
import com.liang.shitucommon.entity.Admin;
import com.liang.shituapi.exception.BusinessException;
import com.liang.shituapi.service.AdminService;
import com.liang.shituapi.util.PasswordUtil;
import com.liang.shitucommon.PageRequest;
import com.liang.shitucommon.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Override
    public Admin getById(Integer id) {
        if (id == null) throw new BusinessException("ID不能为空");
        Admin admin = adminMapper.selectById(id);
        if (admin != null) admin.setPassword(null);
        return admin;
    }

    @Override
    public List<Admin> getAll() {
        List<Admin> list = adminMapper.selectAll();
        list.forEach(admin -> admin.setPassword(null));
        return list;
    }

    @Override
    public boolean add(Admin admin) {
        if (admin == null) throw new BusinessException("参数不能为空");
        if (admin.getUsername() == null || admin.getUsername().trim().isEmpty())
            throw new BusinessException("用户名不能为空");
        if (admin.getPassword() == null || admin.getPassword().trim().isEmpty())
            throw new BusinessException("密码不能为空");

        if (checkUsernameExists(admin.getUsername(), null))
            throw new BusinessException("用户名已存在");

        admin.setPassword(PasswordUtil.encrypt(admin.getPassword()));
        if (admin.getStatus() == null) admin.setStatus(1);
        if (admin.getRole() == null) admin.setRole("admin");

        return adminMapper.insert(admin) > 0;
    }

    @Override
    public boolean addBatch(List<Admin> list) {
        if (list == null || list.isEmpty()) throw new BusinessException("列表不能为空");
        for (Admin admin : list) {
            if (admin.getPassword() != null) {
                admin.setPassword(PasswordUtil.encrypt(admin.getPassword()));
            }
        }
        return adminMapper.batchInsert(list) > 0;
    }

    @Override
    public boolean update(Admin admin) {
        if (admin == null || admin.getId() == null) throw new BusinessException("参数错误");
        if (admin.getUsername() != null) {
            if (checkUsernameExists(admin.getUsername(), admin.getId()))
                throw new BusinessException("用户名已存在");
        }
        if (admin.getPassword() != null) {
            admin.setPassword(PasswordUtil.encrypt(admin.getPassword()));
        }
        return adminMapper.updateById(admin) > 0;
    }

    @Override
    public boolean deleteById(Integer id) {
        if (id == null) throw new BusinessException("ID不能为空");
        if (id == 1) throw new BusinessException("不能删除超级管理员");
        return adminMapper.deleteById(id) > 0;
    }

    @Override
    public boolean deleteBatch(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) throw new BusinessException("ID列表不能为空");
        if (ids.contains(1)) throw new BusinessException("不能删除超级管理员");
        return adminMapper.batchDeleteByIds(ids) > 0;
    }

    @Override
    public Admin login(String username, String password, String ip) {
        if (username == null || password == null) throw new BusinessException("用户名或密码不能为空");
        String encryptedPwd = PasswordUtil.encrypt(password);
        Admin admin = adminMapper.selectByUsernameAndPassword(username, encryptedPwd);
        if (admin == null) throw new BusinessException("用户名或密码错误");
        if (admin.getStatus() == 0) throw new BusinessException("账号已被禁用");

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        adminMapper.updateLastLoginInfo(admin.getId(), now.format(formatter), ip);
        admin.setPassword(null);
        return admin;
    }

    @Override
    public Admin getByUsername(String username) {
        if (username == null) throw new BusinessException("用户名不能为空");
        Admin admin = adminMapper.selectByUsername(username);
        if (admin != null) admin.setPassword(null);
        return admin;
    }

    @Override
    public PageResult<Admin> page(PageRequest pageRequest, String keyword, Integer status) {
        int offset = (pageRequest.getPageNum() - 1) * pageRequest.getPageSize();
        List<Admin> list = adminMapper.selectPage(keyword, status, offset, pageRequest.getPageSize());
        Long total = adminMapper.selectCount(keyword, status);
        int pages = (int) Math.ceil((double) total / pageRequest.getPageSize());
        list.forEach(admin -> admin.setPassword(null));

        PageResult<Admin> result = new PageResult<>();
        result.setList(list); result.setTotal(total); result.setPages(pages);
        result.setPageNum(pageRequest.getPageNum()); result.setPageSize(pageRequest.getPageSize());
        return result;
    }

    @Override
    public boolean changePassword(Integer id, String oldPassword, String newPassword) {
        if (id == null || oldPassword == null || newPassword == null)
            throw new BusinessException("参数不能为空");
        Admin admin = adminMapper.selectById(id);
        if (admin == null) throw new BusinessException("用户不存在");
        if (!PasswordUtil.verify(oldPassword, admin.getPassword()))
            throw new BusinessException("原密码错误");
        String encryptedNewPwd = PasswordUtil.encrypt(newPassword);
        return adminMapper.updatePassword(id, encryptedNewPwd) > 0;
    }

    @Override
    public boolean resetPassword(Integer id) {
        if (id == null) throw new BusinessException("ID不能为空");
        String defaultPassword = "123456";
        String encryptedPwd = PasswordUtil.encrypt(defaultPassword);
        return adminMapper.updatePassword(id, encryptedPwd) > 0;
    }

    @Override
    public boolean updateStatus(Integer id, Integer status) {
        if (id == null || status == null) throw new BusinessException("参数不能为空");
        if (id == 1 && status == 0) throw new BusinessException("不能禁用超级管理员");
        return adminMapper.updateStatus(id, status) > 0;
    }

    @Override
    public boolean checkUsernameExists(String username, Integer excludeId) {
        return adminMapper.checkUsernameExists(username, excludeId) > 0;
    }
}