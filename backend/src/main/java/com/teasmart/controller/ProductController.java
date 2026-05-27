package com.teasmart.controller;

import com.teasmart.common.Result;
import com.teasmart.service.ProductService;
import com.teasmart.vo.ProductVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public Result<List<ProductVO>> list(@RequestParam(required = false) Long categoryId) {
        return Result.ok(productService.listByCategory(categoryId));
    }

    @GetMapping("/products/{id}")
    public Result<ProductVO> detail(@PathVariable Long id) {
        return Result.ok(productService.getDetail(id));
    }
}
