package com.teasmart.dto;

import jakarta.validation.constraints.NotBlank;

public class BannerDTO {

    @NotBlank(message = "图片不能为空")
    private String image;

    private String link;

    private Integer sortOrder = 0;

    private Integer status = 1;

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
