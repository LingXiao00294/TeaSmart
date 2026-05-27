package com.teasmart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CartDTO {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    private List<Long> specIds;

    @Min(value = 1, message = "数量至少为1")
    private int quantity = 1;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public List<Long> getSpecIds() { return specIds; }
    public void setSpecIds(List<Long> specIds) { this.specIds = specIds; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
