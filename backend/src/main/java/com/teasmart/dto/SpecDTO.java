package com.teasmart.dto;

import java.math.BigDecimal;

public class SpecDTO {

    private String specType;
    private String specName;
    private BigDecimal priceDiff = BigDecimal.ZERO;

    public String getSpecType() { return specType; }
    public void setSpecType(String specType) { this.specType = specType; }
    public String getSpecName() { return specName; }
    public void setSpecName(String specName) { this.specName = specName; }
    public BigDecimal getPriceDiff() { return priceDiff; }
    public void setPriceDiff(BigDecimal priceDiff) { this.priceDiff = priceDiff; }
}
