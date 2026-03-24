package com.liang.shituapi.dao;


import com.liang.shitucommon.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分类数据访问接口
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category, Integer> {

    /**
     * 根据编码查询分类
     */
    com.liang.shitucommon.entity.Category selectByCode(@Param("code") String code);

    /**
     * 查询所有启用的分类
     */
    List<com.liang.shitucommon.entity.Category> selectEnabled();

    /**
     * 分页查询分类列表
     */
    List<com.liang.shitucommon.entity.Category> selectPage(@Param("keyword") String keyword,
                                                           @Param("status") Integer status,
                                                           @Param("offset") Integer offset,
                                                           @Param("limit") Integer limit);

    /**
     * 查询总数
     */
    Long selectCount(@Param("keyword") String keyword,
                     @Param("status") Integer status);

    /**
     * 更新状态
     */
    int updateStatus(@Param("id") Integer id,
                     @Param("status") Integer status);

    /**
     * 检查编码是否存在
     */
    int checkCodeExists(@Param("code") String code,
                        @Param("excludeId") Integer excludeId);

    /**
     * 获取最大排序值
     */
    Integer getMaxSort();

    /**
     * 更新排序
     */
    int updateSort(@Param("id") Integer id,
                   @Param("sort") Integer sort);
}
