package com.teasmart.controller;

import com.teasmart.common.Result;
import com.teasmart.dto.CartDTO;
import com.teasmart.service.CartService;
import com.teasmart.vo.CartItemVO;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    public Result<CartItemVO> add(Authentication auth, @Valid @RequestBody CartDTO dto) {
        Long userId = (Long) auth.getPrincipal();
        return Result.ok(cartService.add(userId, dto));
    }

    @GetMapping
    public Result<List<CartItemVO>> list(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.ok(cartService.list(userId));
    }

    @PutMapping("/{id}")
    public Result<Void> updateQuantity(Authentication auth,
                                        @PathVariable Long id,
                                        @RequestBody CartDTO dto) {
        Long userId = (Long) auth.getPrincipal();
        cartService.updateQuantity(id, userId, dto.getQuantity());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteItem(Authentication auth, @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        cartService.deleteItem(id, userId);
        return Result.ok();
    }

    @DeleteMapping
    public Result<Void> clear(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        cartService.clear(userId);
        return Result.ok();
    }
}
