package com.teasmart.dto;

import jakarta.validation.constraints.NotBlank;

public class CategoryDTO {

    @NotBlank(message = "分类名不能为空")
    private String name;

    private Integer sortOrder = 0;

    private Integer status = 1;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
