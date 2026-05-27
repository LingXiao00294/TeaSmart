package com.teasmart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public class ProductDTO {

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotBlank(message = "商品名不能为空")
    private String name;

    private String description;

    private String image;

    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    private Integer status = 1;

    private List<SpecDTO> specs;

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public List<SpecDTO> getSpecs() { return specs; }
    public void setSpecs(List<SpecDTO> specs) { this.specs = specs; }
}
