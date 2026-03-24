package com.liang.shituapi.dao;


import com.liang.shitucommon.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 管理员数据访问接口
 */
@Mapper
public interface AdminMapper extends BaseMapper<Admin, Integer> {

    /**
     * 根据用户名查询管理员
     */
    com.liang.shitucommon.entity.Admin selectByUsername(@Param("username") String username);

    /**
     * 根据用户名和密码查询（登录）
     */
    com.liang.shitucommon.entity.Admin selectByUsernameAndPassword(@Param("username") String username,
                                                                   @Param("password") String password);

    /**
     * 更新最后登录信息
     */
    int updateLastLoginInfo(@Param("id") Integer id,
                            @Param("lastLoginTime") String lastLoginTime,
                            @Param("lastLoginIp") String lastLoginIp);

    /**
     * 分页查询管理员列表
     */
    List<com.liang.shitucommon.entity.Admin> selectPage(@Param("keyword") String keyword,
                                                        @Param("status") Integer status,
                                                        @Param("offset") Integer offset,
                                                        @Param("limit") Integer limit);

    /**
     * 查询总数
     */
    Long selectCount(@Param("keyword") String keyword,
                     @Param("status") Integer status);

    /**
     * 更新密码
     */
    int updatePassword(@Param("id") Integer id,
                       @Param("newPassword") String newPassword);

    /**
     * 更新状态
     */
    int updateStatus(@Param("id") Integer id,
                     @Param("status") Integer status);

    /**
     * 检查用户名是否存在
     */
    int checkUsernameExists(@Param("username") String username,
                            @Param("excludeId") Integer excludeId);
}