package com.teasmart.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.teasmart.entity.Order;
import com.teasmart.entity.Product;
import com.teasmart.entity.User;
import com.teasmart.mapper.OrderMapper;
import com.teasmart.mapper.ProductMapper;
import com.teasmart.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {

    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;

    public DashboardService(OrderMapper orderMapper, UserMapper userMapper, ProductMapper productMapper) {
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
        this.productMapper = productMapper;
    }

    public Map<String, Object> getStats() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        Long todayOrders = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().ge(Order::getCreatedAt, todayStart));

        BigDecimal todaySales = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .ge(Order::getCreatedAt, todayStart)
                        .in(Order::getStatus, 1, 2, 3))
                .stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long totalUsers = userMapper.selectCount(null);
        Long totalProducts = productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getDeleted, 0));

        Map<String, Object> stats = new HashMap<>();
        stats.put("todayOrderCount", todayOrders);
        stats.put("todaySalesAmount", todaySales);
        stats.put("totalUserCount", totalUsers);
        stats.put("totalProductCount", totalProducts);
        return stats;
    }
}
