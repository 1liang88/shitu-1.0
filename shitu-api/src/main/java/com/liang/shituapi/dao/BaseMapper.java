package com.liang.shituapi.dao;

import com.liang.shitucommon.entity.Place;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 基础Mapper接口，定义通用CRUD方法
 */
public interface BaseMapper<T, ID> {

    /**
     * 根据ID查询
     */
    T selectById(@Param("id") ID id);

    /**
     * 查询所有
     */
    List<T> selectAll();

    /**
     * 插入
     */
    int insert(T entity);

    /**
     * 批量插入
     */
    int batchInsert(List<T> list);

    /**
     * 根据ID更新
     */
    int updateById(T entity);

    /**
     * 根据ID删除
     */
    int deleteById(@Param("id") ID id);

    /**
     * 批量删除
     */
    int batchDeleteByIds(@Param("ids") List<ID> ids);
}