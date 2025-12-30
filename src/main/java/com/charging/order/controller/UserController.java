package com.charging.order.controller;


import com.charging.order.entity.SysUser;
import com.charging.order.service.IUserService;
import org.apache.catalina.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @PostMapping("/login")
    public String login(@RequestBody SysUser user){

        boolean success = userService.login(user.getUsername(),user.getPassword());

        if(success){
            return "登陆成功！Token：暂时假装有一个Token";
        }else{
            return "登陆失败，账号或者密码错误！";
        }
    }

    @PostMapping("/register")
    public String register(@RequestBody SysUser user){
        boolean success = userService.register(user);

        if(success){
            return "注册成功！";
        }else{
            return "注册失败，用户名可能已经存在！";
        }
    }

}
