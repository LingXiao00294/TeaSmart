package com.teasmart.vo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ProductVO {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String description;
    private String image;
    private BigDecimal price;
    private Integer status;
    private Map<String, List<SpecVO>> specs;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
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
    public Map<String, List<SpecVO>> getSpecs() { return specs; }
    public void setSpecs(Map<String, List<SpecVO>> specs) { this.specs = specs; }
}
