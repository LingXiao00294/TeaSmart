package com.teasmart.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teasmart.common.Result;
import com.teasmart.dto.BannerDTO;
import com.teasmart.dto.CategoryDTO;
import com.teasmart.dto.OrderStatusDTO;
import com.teasmart.dto.ProductDTO;
import com.teasmart.entity.Banner;
import com.teasmart.entity.Category;
import com.teasmart.entity.Order;
import com.teasmart.entity.Product;
import com.teasmart.entity.User;
import com.teasmart.mapper.UserMapper;
import com.teasmart.service.BannerService;
import com.teasmart.service.CategoryService;
import com.teasmart.service.DashboardService;
import com.teasmart.service.OrderService;
import com.teasmart.service.ProductService;
import com.teasmart.vo.OrderVO;
import com.teasmart.vo.ProductVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final CategoryService categoryService;
    private final ProductService productService;
    private final OrderService orderService;
    private final BannerService bannerService;
    private final DashboardService dashboardService;
    private final UserMapper userMapper;

    public AdminController(CategoryService categoryService,
                           ProductService productService,
                           OrderService orderService,
                           BannerService bannerService,
                           DashboardService dashboardService,
                           UserMapper userMapper) {
        this.categoryService = categoryService;
        this.productService = productService;
        this.orderService = orderService;
        this.bannerService = bannerService;
        this.dashboardService = dashboardService;
        this.userMapper = userMapper;
    }

    // ========== Dashboard ==========

    @GetMapping("/dashboard/stats")
    public Result<Map<String, Object>> dashboardStats() {
        return Result.ok(dashboardService.getStats());
    }

    // ========== 上传 ==========

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        return Result.ok(bannerService.upload(file));
    }

    // ========== 分类管理 ==========

    @GetMapping("/categories")
    public Result<List<Category>> listCategories() {
        return Result.ok(categoryService.listAll());
    }

    @PostMapping("/categories")
    public Result<Category> createCategory(@Valid @RequestBody CategoryDTO dto) {
        return Result.ok(categoryService.create(dto));
    }

    @PutMapping("/categories/{id}")
    public Result<Category> updateCategory(@PathVariable Long id,
                                           @Valid @RequestBody CategoryDTO dto) {
        return Result.ok(categoryService.update(id, dto));
    }

    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.ok();
    }

    // ========== 商品管理 ==========

    @GetMapping("/products")
    public Result<Page<Product>> listProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(productService.listAdmin(categoryId, keyword, page, size));
    }

    @PostMapping("/products")
    public Result<ProductVO> createProduct(@Valid @RequestBody ProductDTO dto) {
        return Result.ok(productService.create(dto));
    }

    @GetMapping("/products/{id}")
    public Result<ProductVO> getProduct(@PathVariable Long id) {
        return Result.ok(productService.getDetail(id));
    }

    @PutMapping("/products/{id}")
    public Result<ProductVO> updateProduct(@PathVariable Long id,
                                           @Valid @RequestBody ProductDTO dto) {
        return Result.ok(productService.update(id, dto));
    }

    @DeleteMapping("/products/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return Result.ok();
    }

    // ========== 订单管理 ==========

    @GetMapping("/orders")
    public Result<Page<Order>> listOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(orderService.listAdmin(status, page, size));
    }

    @PutMapping("/orders/{id}/status")
    public Result<OrderVO> updateOrderStatus(@PathVariable Long id,
                                              @RequestBody OrderStatusDTO dto) {
        return Result.ok(orderService.updateStatus(id, dto.getStatus()));
    }

    // ========== 用户管理 ==========

    @GetMapping("/users")
    public Result<Page<User>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<User> result = userMapper.selectPage(new Page<>(page, size), null);
        // 清除密码
        result.getRecords().forEach(u -> u.setPassword(null));
        return Result.ok(result);
    }

    // ========== 轮播图管理 ==========

    @GetMapping("/banners")
    public Result<List<Banner>> listBanners() {
        return Result.ok(bannerService.listAll());
    }

    @PostMapping("/banners")
    public Result<Banner> createBanner(@Valid @RequestBody BannerDTO dto) {
        return Result.ok(bannerService.create(dto));
    }

    @PutMapping("/banners/{id}")
    public Result<Banner> updateBanner(@PathVariable Long id,
                                        @Valid @RequestBody BannerDTO dto) {
        return Result.ok(bannerService.update(id, dto));
    }

    @DeleteMapping("/banners/{id}")
    public Result<Void> deleteBanner(@PathVariable Long id) {
        bannerService.delete(id);
        return Result.ok();
    }
}
