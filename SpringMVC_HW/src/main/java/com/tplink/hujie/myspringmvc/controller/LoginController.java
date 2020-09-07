/*
 * Copyright (c) 2020, TP-Link Co.,Ltd.
 * Author: hujie <hujie1@tp-link.com.cn>
 * Created: 2020-09-01
 */
package com.tplink.hujie.myspringmvc.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import com.tplink.hujie.myspringmvc.annotation.MyAutowired;
import com.tplink.hujie.myspringmvc.annotation.MyController;
import com.tplink.hujie.myspringmvc.annotation.MyRequestMapping;
import com.tplink.hujie.myspringmvc.annotation.MyRequestParam;
import com.tplink.hujie.myspringmvc.service.UserService;
import com.tplink.hujie.myspringmvc.service.UserServiceImpl;


/**
 * @author Hujie
 */
@MyController
@MyRequestMapping("/user")
public class LoginController {

    @MyAutowired
    private UserServiceImpl userService;

    @MyRequestMapping("/login")
    public String login(HttpServletRequest request, HttpServletResponse response,
                      @MyRequestParam("name") String name, @MyRequestParam("password") String password){
        System.out.println("/login: name: "+name + ", password: "+password);
        String resp;
        if(userService.userLogin(name,password)){
            resp ="login successful";
        }else {
            resp = "login failed";
        }

        return resp;
    }

    @MyRequestMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response,
                       @MyRequestParam("name") String name, @MyRequestParam("password") String password){
        String resp;
        System.out.println("/logout: name: "+name + ", password: "+password);
        if(userService.userLogout(name,password)){
            resp ="logout successful";
        }else {
            resp = "logout failed";
        }
        return resp;
    }
}