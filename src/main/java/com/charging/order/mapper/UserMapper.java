package com.charging.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.charging.order.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<SysUser> {
}
