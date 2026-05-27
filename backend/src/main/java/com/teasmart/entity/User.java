package com.teasmart.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("app_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String phone;

    private String avatar;

    private String role;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
