package com.teasmart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teasmart.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
