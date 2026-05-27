package com.teasmart.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.teasmart.common.BusinessException;
import com.teasmart.dto.CategoryDTO;
import com.teasmart.entity.Category;
import com.teasmart.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public List<Category> listActive() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, 1)
                        .eq(Category::getDeleted, 0)
                        .orderByAsc(Category::getSortOrder));
    }

    public List<Category> listAll() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getDeleted, 0)
                        .orderByAsc(Category::getSortOrder));
    }

    public Category create(CategoryDTO dto) {
        Category cat = new Category();
        cat.setName(dto.getName());
        cat.setSortOrder(dto.getSortOrder());
        cat.setStatus(dto.getStatus());
        categoryMapper.insert(cat);
        return cat;
    }

    public Category update(Long id, CategoryDTO dto) {
        Category cat = categoryMapper.selectById(id);
        if (cat == null || cat.getDeleted() == 1) {
            throw BusinessException.notFound("分类不存在");
        }
        cat.setName(dto.getName());
        cat.setSortOrder(dto.getSortOrder());
        cat.setStatus(dto.getStatus());
        categoryMapper.updateById(cat);
        return cat;
    }

    public void delete(Long id) {
        Category cat = categoryMapper.selectById(id);
        if (cat == null || cat.getDeleted() == 1) {
            throw BusinessException.notFound("分类不存在");
        }
        cat.setDeleted(1);
        categoryMapper.updateById(cat);
    }
}
