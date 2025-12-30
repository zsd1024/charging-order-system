package com.charging.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.charging.order.entity.SysUser;
import com.charging.order.mapper.UserMapper;
import com.charging.order.service.IUserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, SysUser> implements IUserService {
    @Override
    public boolean login(String username, String password) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername,username);

        SysUser user = this.getOne(wrapper);

        if(user == null){
            return false;
        }
        return user.getPassword().equals(password);
    }

    @Override
    public boolean register(SysUser user) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, user.getUsername());

        long count = this.count(wrapper);
        if(count > 0){
            return false;
        }
        return this.save(user);
    }
}
