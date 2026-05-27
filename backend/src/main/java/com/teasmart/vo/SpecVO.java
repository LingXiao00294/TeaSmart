package com.teasmart.vo;

import java.math.BigDecimal;

public class SpecVO {

    private Long id;
    private String specType;
    private String specName;
    private BigDecimal priceDiff;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSpecType() { return specType; }
    public void setSpecType(String specType) { this.specType = specType; }
    public String getSpecName() { return specName; }
    public void setSpecName(String specName) { this.specName = specName; }
    public BigDecimal getPriceDiff() { return priceDiff; }
    public void setPriceDiff(BigDecimal priceDiff) { this.priceDiff = priceDiff; }
}
