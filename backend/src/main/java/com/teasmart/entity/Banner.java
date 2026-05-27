package com.teasmart.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("banner")
public class Banner {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String image;

    private String link;

    private Integer sortOrder;

    private Integer status;

    private Integer deleted;
}
