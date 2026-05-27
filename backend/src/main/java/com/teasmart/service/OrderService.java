package com.teasmart.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teasmart.common.BusinessException;
import com.teasmart.entity.*;
import com.teasmart.mapper.*;
import com.teasmart.vo.OrderItemVO;
import com.teasmart.vo.OrderVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Map<Integer, String> STATUS_TEXT = Map.of(
            0, "待支付", 1, "已支付", 2, "制作中", 3, "已完成", 4, "已取消");

    private static final Map<Integer, Set<Integer>> ALLOWED_TRANSITIONS = Map.of(
            0, Set.of(1, 4),  // 待支付 → 已支付/已取消
            1, Set.of(2),     // 已支付 → 制作中
            2, Set.of(3)      // 制作中 → 已完成
    );

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartItemMapper cartItemMapper;
    private final CartItemSpecMapper cartItemSpecMapper;
    private final ProductMapper productMapper;
    private final ProductSpecMapper productSpecMapper;

    private static final AtomicLong ORDER_SEQ = new AtomicLong(System.currentTimeMillis() % 10000);

    public OrderService(OrderMapper orderMapper,
                        OrderItemMapper orderItemMapper,
                        CartItemMapper cartItemMapper,
                        CartItemSpecMapper cartItemSpecMapper,
                        ProductMapper productMapper,
                        ProductSpecMapper productSpecMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.cartItemMapper = cartItemMapper;
        this.cartItemSpecMapper = cartItemSpecMapper;
        this.productMapper = productMapper;
        this.productSpecMapper = productSpecMapper;
    }

    @Transactional
    public OrderVO create(Long userId, String remark) {
        List<CartItem> cartItems = cartItemMapper.selectList(
                new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, userId));
        if (cartItems.isEmpty()) {
            throw BusinessException.badRequest("购物车为空");
        }

        // 生成订单号
        String orderNo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", ORDER_SEQ.incrementAndGet() % 10000);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem ci : cartItems) {
            Product product = productMapper.selectById(ci.getProductId());
            if (product == null) {
                throw BusinessException.badRequest("商品不存在");
            }

            // 获取规格信息
            List<CartItemSpec> cartSpecs = cartItemSpecMapper.selectList(
                    new LambdaQueryWrapper<CartItemSpec>().eq(CartItemSpec::getCartItemId, ci.getId()));
            List<ProductSpec> specs = new ArrayList<>();
            if (!cartSpecs.isEmpty()) {
                List<Long> specIds = cartSpecs.stream()
                        .map(CartItemSpec::getSpecId).collect(Collectors.toList());
                specs = productSpecMapper.selectBatchIds(specIds);
            }

            String specInfo = specs.stream()
                    .map(ProductSpec::getSpecName).collect(Collectors.joining(" / "));
            BigDecimal specDiff = specs.stream()
                    .map(ProductSpec::getPriceDiff)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal unitPrice = product.getPrice().add(specDiff);
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(ci.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem oi = new OrderItem();
            oi.setProductId(product.getId());
            oi.setProductName(product.getName());
            oi.setSpecInfo(specInfo);
            oi.setPrice(unitPrice);
            oi.setQuantity(ci.getQuantity());
            orderItems.add(oi);
        }

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(0);
        order.setRemark(remark);
        orderMapper.insert(order);

        for (OrderItem oi : orderItems) {
            oi.setOrderId(order.getId());
            orderItemMapper.insert(oi);
        }

        // 清空购物车
        for (CartItem ci : cartItems) {
            cartItemSpecMapper.delete(
                    new LambdaQueryWrapper<CartItemSpec>().eq(CartItemSpec::getCartItemId, ci.getId()));
        }
        cartItemMapper.delete(
                new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, userId));

        return toVO(order);
    }

    @Transactional
    public OrderVO pay(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw BusinessException.notFound("订单不存在");
        }
        checkTransition(order.getStatus(), 1);
        order.setStatus(1);
        orderMapper.updateById(order);
        return toVO(order);
    }

    @Transactional
    public OrderVO cancel(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw BusinessException.notFound("订单不存在");
        }
        checkTransition(order.getStatus(), 4);
        order.setStatus(4);
        orderMapper.updateById(order);
        return toVO(order);
    }

    public List<OrderVO> listByUser(Long userId, Integer status) {
        LambdaQueryWrapper<Order> qw = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreatedAt);
        if (status != null) {
            qw.eq(Order::getStatus, status);
        }
        return orderMapper.selectList(qw).stream()
                .map(this::toVO).collect(Collectors.toList());
    }

    public OrderVO getDetail(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw BusinessException.notFound("订单不存在");
        }
        return toVO(order);
    }

    public Page<Order> listAdmin(Integer status, int page, int size) {
        LambdaQueryWrapper<Order> qw = new LambdaQueryWrapper<Order>()
                .orderByDesc(Order::getCreatedAt);
        if (status != null) {
            qw.eq(Order::getStatus, status);
        }
        return orderMapper.selectPage(new Page<>(page, size), qw);
    }

    @Transactional
    public OrderVO updateStatus(Long orderId, Integer newStatus) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw BusinessException.notFound("订单不存在");
        }
        checkTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);
        orderMapper.updateById(order);
        return toVO(order);
    }

    private void checkTransition(int from, int to) {
        Set<Integer> allowed = ALLOWED_TRANSITIONS.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw BusinessException.badRequest(
                    "不允许从「" + STATUS_TEXT.get(from) + "」变更为「" + STATUS_TEXT.get(to) + "」");
        }
    }

    private OrderVO toVO(Order order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setStatusText(STATUS_TEXT.getOrDefault(order.getStatus(), "未知"));
        vo.setRemark(order.getRemark());
        vo.setCreatedAt(order.getCreatedAt());

        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        vo.setItems(items.stream().map(this::toItemVO).collect(Collectors.toList()));

        return vo;
    }

    private OrderItemVO toItemVO(OrderItem item) {
        OrderItemVO vo = new OrderItemVO();
        vo.setProductId(item.getProductId());
        vo.setProductName(item.getProductName());
        vo.setSpecInfo(item.getSpecInfo());
        vo.setPrice(item.getPrice());
        vo.setQuantity(item.getQuantity());
        vo.setSubtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        return vo;
    }
}
