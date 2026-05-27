package com.teasmart.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("product_spec")
public class ProductSpec {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;

    private String specType;

    private String specName;

    private BigDecimal priceDiff;
}
