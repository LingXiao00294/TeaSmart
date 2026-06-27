package com.teasmart.dto;

import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @Size(max = 20, message = "手机号长度不能超过20")
    private String phone;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
