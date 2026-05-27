package com.teasmart.vo;

import java.math.BigDecimal;

public class CartItemVO {

    private Long id;
    private Long productId;
    private String productName;
    private String image;
    private String specInfo;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal subtotal;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getSpecInfo() { return specInfo; }
    public void setSpecInfo(String specInfo) { this.specInfo = specInfo; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
