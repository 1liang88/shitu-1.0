package com.liang.shituapi.service;

import com.liang.shitucommon.entity.Category;
import com.liang.shitucommon.PageRequest;
import com.liang.shitucommon.PageResult;
import java.util.List;

public interface CategoryService extends BaseService<Category, Integer> {
    Category getByCode(String code);
    List<Category> getEnabled();
    PageResult<Category> page(PageRequest pageRequest, String keyword, Integer status);
    boolean updateStatus(Integer id, Integer status);
    boolean checkCodeExists(String code, Integer excludeId);
    boolean updateSort(Integer id, Integer sort);
}