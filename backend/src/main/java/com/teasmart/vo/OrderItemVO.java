package com.teasmart.vo;

import java.math.BigDecimal;

public class OrderItemVO {

    private Long productId;
    private String productName;
    private String specInfo;
    private BigDecimal price;
    private int quantity;
    private BigDecimal subtotal;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getSpecInfo() { return specInfo; }
    public void setSpecInfo(String specInfo) { this.specInfo = specInfo; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
