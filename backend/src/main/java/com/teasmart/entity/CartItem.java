package com.teasmart.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("cart_item")
public class CartItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long productId;

    private String specKey;

    private Integer quantity;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
