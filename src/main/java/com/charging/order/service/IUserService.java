package com.charging.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.charging.order.entity.SysUser;

public interface IUserService extends IService<SysUser> {

    boolean login(String username,String password);

    boolean register(SysUser user);


}
