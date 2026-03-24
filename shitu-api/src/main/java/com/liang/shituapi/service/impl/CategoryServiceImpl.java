package com.liang.shituapi.service.impl;

import com.liang.shituapi.dao.CategoryMapper;
import com.liang.shitucommon.entity.Category;
import com.liang.shituapi.exception.BusinessException;
import com.liang.shituapi.service.CategoryService;
import com.liang.shitucommon.PageRequest;
import com.liang.shitucommon.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public Category getById(Integer id) {
        if (id == null) throw new BusinessException("ID不能为空");
        return categoryMapper.selectById(id);
    }

    @Override
    public List<Category> getAll() {
        return categoryMapper.selectAll();
    }

    @Override
    public boolean add(Category category) {
        if (category == null) throw new BusinessException("参数不能为空");
        if (category.getName() == null || category.getName().trim().isEmpty())
            throw new BusinessException("分类名称不能为空");
        if (category.getCode() == null || category.getCode().trim().isEmpty())
            throw new BusinessException("分类编码不能为空");
        if (checkCodeExists(category.getCode(), null))
            throw new BusinessException("分类编码已存在");
        if (category.getStatus() == null) category.setStatus(1);
        if (category.getSort() == null) {
            Integer maxSort = categoryMapper.getMaxSort();
            category.setSort(maxSort + 1);
        }
        return categoryMapper.insert(category) > 0;
    }

    @Override
    public boolean addBatch(List<Category> list) {
        if (list == null || list.isEmpty()) throw new BusinessException("列表不能为空");
        Integer maxSort = categoryMapper.getMaxSort();
        for (int i = 0; i < list.size(); i++) {
            Category category = list.get(i);
            if (category.getSort() == null) {
                category.setSort(maxSort + i + 1);
            }
        }
        return categoryMapper.batchInsert(list) > 0;
    }

    @Override
    public boolean update(Category category) {
        if (category == null || category.getId() == null) throw new BusinessException("参数错误");
        if (category.getCode() != null) {
            if (checkCodeExists(category.getCode(), category.getId()))
                throw new BusinessException("分类编码已存在");
        }
        return categoryMapper.updateById(category) > 0;
    }

    @Override
    public boolean deleteById(Integer id) {
        if (id == null) throw new BusinessException("ID不能为空");
        // TODO: 检查是否被点位使用
        return categoryMapper.deleteById(id) > 0;
    }

    @Override
    public boolean deleteBatch(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) throw new BusinessException("ID列表不能为空");
        return categoryMapper.batchDeleteByIds(ids) > 0;
    }

    @Override
    public Category getByCode(String code) {
        if (code == null) throw new BusinessException("编码不能为空");
        return categoryMapper.selectByCode(code);
    }

    @Override
    public List<Category> getEnabled() {
        return categoryMapper.selectEnabled();
    }

    @Override
    public PageResult<Category> page(PageRequest pageRequest, String keyword, Integer status) {
        int offset = (pageRequest.getPageNum() - 1) * pageRequest.getPageSize();
        List<Category> list = categoryMapper.selectPage(keyword, status, offset, pageRequest.getPageSize());
        Long total = categoryMapper.selectCount(keyword, status);
        int pages = (int) Math.ceil((double) total / pageRequest.getPageSize());

        PageResult<Category> result = new PageResult<>();
        result.setList(list); result.setTotal(total); result.setPages(pages);
        result.setPageNum(pageRequest.getPageNum()); result.setPageSize(pageRequest.getPageSize());
        return result;
    }

    @Override
    public boolean updateStatus(Integer id, Integer status) {
        if (id == null || status == null) throw new BusinessException("参数不能为空");
        return categoryMapper.updateStatus(id, status) > 0;
    }

    @Override
    public boolean checkCodeExists(String code, Integer excludeId) {
        return categoryMapper.checkCodeExists(code, excludeId) > 0;
    }

    @Override
    public boolean updateSort(Integer id, Integer sort) {
        if (id == null || sort == null) throw new BusinessException("参数不能为空");
        return categoryMapper.updateSort(id, sort) > 0;
    }
}