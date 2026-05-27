package com.teasmart.controller;

import com.teasmart.common.Result;
import com.teasmart.entity.Category;
import com.teasmart.service.CategoryService;
import com.teasmart.service.ProductService;
import com.teasmart.vo.ProductVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    public Result<List<Category>> list() {
        return Result.ok(categoryService.listActive());
    }
}
