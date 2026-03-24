package com.liang.shituapi.service;

import java.util.List;

/**
 * 通用Service接口
 */
public interface BaseService<T, ID> {
    T getById(ID id);
    List<T> getAll();
    boolean add(T entity);
    boolean addBatch(List<T> list);
    boolean update(T entity);
    boolean deleteById(ID id);
    boolean deleteBatch(List<ID> ids);
}