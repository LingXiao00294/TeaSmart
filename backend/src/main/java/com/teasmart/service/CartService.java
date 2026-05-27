package com.teasmart.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.teasmart.common.BusinessException;
import com.teasmart.dto.CartDTO;
import com.teasmart.entity.*;
import com.teasmart.mapper.*;
import com.teasmart.vo.CartItemVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartItemMapper cartItemMapper;
    private final CartItemSpecMapper cartItemSpecMapper;
    private final ProductMapper productMapper;
    private final ProductSpecMapper productSpecMapper;

    public CartService(CartItemMapper cartItemMapper,
                       CartItemSpecMapper cartItemSpecMapper,
                       ProductMapper productMapper,
                       ProductSpecMapper productSpecMapper) {
        this.cartItemMapper = cartItemMapper;
        this.cartItemSpecMapper = cartItemSpecMapper;
        this.productMapper = productMapper;
        this.productSpecMapper = productSpecMapper;
    }

    @Transactional
    public CartItemVO add(Long userId, CartDTO dto) {
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null || product.getDeleted() == 1 || product.getStatus() != 1) {
            throw BusinessException.badRequest("商品不存在或已下架");
        }

        List<Long> specIds = dto.getSpecIds() != null ? dto.getSpecIds() : Collections.emptyList();
        validateSpecs(product.getId(), specIds);

        // 生成 specKey（排序后拼接）
        List<Long> sorted = new ArrayList<>(specIds);
        Collections.sort(sorted);
        String specKey = sorted.stream().map(String::valueOf).collect(Collectors.joining(","));

        // 检查是否已存在相同商品+规格的购物车项
        CartItem existing = cartItemMapper.selectOne(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getUserId, userId)
                        .eq(CartItem::getProductId, product.getId())
                        .eq(CartItem::getSpecKey, specKey));

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + dto.getQuantity());
            cartItemMapper.updateById(existing);
            return toVO(existing);
        }

        CartItem item = new CartItem();
        item.setUserId(userId);
        item.setProductId(product.getId());
        item.setSpecKey(specKey);
        item.setQuantity(dto.getQuantity());
        cartItemMapper.insert(item);

        // 保存规格关联
        for (Long specId : specIds) {
            CartItemSpec cis = new CartItemSpec();
            cis.setCartItemId(item.getId());
            cis.setSpecId(specId);
            cartItemSpecMapper.insert(cis);
        }

        return toVO(item);
    }

    public List<CartItemVO> list(Long userId) {
        List<CartItem> items = cartItemMapper.selectList(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getUserId, userId)
                        .orderByDesc(CartItem::getCreatedAt));
        return items.stream().map(this::toVO).collect(Collectors.toList());
    }

    public void updateQuantity(Long cartItemId, Long userId, int quantity) {
        CartItem item = cartItemMapper.selectById(cartItemId);
        if (item == null || !item.getUserId().equals(userId)) {
            throw BusinessException.notFound("购物车项不存在");
        }
        item.setQuantity(quantity);
        cartItemMapper.updateById(item);
    }

    public void deleteItem(Long cartItemId, Long userId) {
        CartItem item = cartItemMapper.selectById(cartItemId);
        if (item == null || !item.getUserId().equals(userId)) {
            throw BusinessException.notFound("购物车项不存在");
        }
        cartItemSpecMapper.delete(
                new LambdaQueryWrapper<CartItemSpec>().eq(CartItemSpec::getCartItemId, cartItemId));
        cartItemMapper.deleteById(cartItemId);
    }

    public void clear(Long userId) {
        List<CartItem> items = cartItemMapper.selectList(
                new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, userId));
        for (CartItem item : items) {
            cartItemSpecMapper.delete(
                    new LambdaQueryWrapper<CartItemSpec>().eq(CartItemSpec::getCartItemId, item.getId()));
        }
        cartItemMapper.delete(
                new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, userId));
    }

    private void validateSpecs(Long productId, List<Long> specIds) {
        if (specIds.isEmpty()) return;

        List<ProductSpec> specs = productSpecMapper.selectList(
                new LambdaQueryWrapper<ProductSpec>().eq(ProductSpec::getProductId, productId));
        Set<Long> validIds = specs.stream().map(ProductSpec::getId).collect(Collectors.toSet());

        for (Long specId : specIds) {
            if (!validIds.contains(specId)) {
                throw BusinessException.badRequest("规格ID " + specId + " 不属于该商品");
            }
        }

        // 检查每个 spec_type 最多选一个
        Map<String, Long> typeCount = specs.stream()
                .filter(s -> specIds.contains(s.getId()))
                .collect(Collectors.groupingBy(ProductSpec::getSpecType, Collectors.counting()));
        for (var entry : typeCount.entrySet()) {
            if (entry.getValue() > 1) {
                throw BusinessException.badRequest("每种规格类型最多选择一个");
            }
        }
    }

    private CartItemVO toVO(CartItem item) {
        Product product = productMapper.selectById(item.getProductId());

        CartItemVO vo = new CartItemVO();
        vo.setId(item.getId());
        vo.setProductId(item.getProductId());
        vo.setQuantity(item.getQuantity());

        if (product != null) {
            vo.setProductName(product.getName());
            vo.setImage(product.getImage());
            vo.setUnitPrice(product.getPrice());
        }

        // 获取规格信息
        List<CartItemSpec> cartSpecs = cartItemSpecMapper.selectList(
                new LambdaQueryWrapper<CartItemSpec>().eq(CartItemSpec::getCartItemId, item.getId()));
        if (!cartSpecs.isEmpty()) {
            List<Long> specIds = cartSpecs.stream().map(CartItemSpec::getSpecId).collect(Collectors.toList());
            List<ProductSpec> specs = productSpecMapper.selectBatchIds(specIds);
            String specInfo = specs.stream()
                    .map(ProductSpec::getSpecName)
                    .collect(Collectors.joining(" / "));
            vo.setSpecInfo(specInfo);

            BigDecimal specDiff = specs.stream()
                    .map(ProductSpec::getPriceDiff)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (product != null) {
                vo.setUnitPrice(product.getPrice().add(specDiff));
            }
        }

        if (vo.getUnitPrice() != null) {
            vo.setSubtotal(vo.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        return vo;
    }
}
