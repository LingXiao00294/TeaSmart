package com.teasmart.controller;

import com.teasmart.common.Result;
import com.teasmart.dto.OrderDTO;
import com.teasmart.service.OrderService;
import com.teasmart.vo.OrderVO;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Result<OrderVO> create(Authentication auth, @RequestBody(required = false) OrderDTO dto) {
        Long userId = (Long) auth.getPrincipal();
        String remark = dto != null ? dto.getRemark() : null;
        return Result.ok(orderService.create(userId, remark));
    }

    @PostMapping("/{id}/pay")
    public Result<OrderVO> pay(Authentication auth, @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        return Result.ok(orderService.pay(id, userId));
    }

    @GetMapping
    public Result<List<OrderVO>> list(Authentication auth,
                                       @RequestParam(required = false) Integer status) {
        Long userId = (Long) auth.getPrincipal();
        return Result.ok(orderService.listByUser(userId, status));
    }

    @GetMapping("/{id}")
    public Result<OrderVO> detail(Authentication auth, @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        return Result.ok(orderService.getDetail(id, userId));
    }

    @PutMapping("/{id}/cancel")
    public Result<OrderVO> cancel(Authentication auth, @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        return Result.ok(orderService.cancel(id, userId));
    }
}
