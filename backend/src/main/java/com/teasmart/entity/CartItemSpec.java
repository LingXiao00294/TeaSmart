package com.teasmart.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("cart_item_spec")
public class CartItemSpec {

    private Long cartItemId;

    private Long specId;
}
