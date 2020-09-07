/*
 * Copyright (c) 2020, TP-Link Co.,Ltd.
 * Author: hujie <hujie1@tp-link.com.cn>
 * Created: 2020-09-02
 */
package com.tplink.hujie.myspringmvc.service;

import java.util.HashMap;
import java.util.Map;

import com.tplink.hujie.myspringmvc.annotation.MyService;

/**
 * @author Hujie
 */

@MyService
public class UserServiceImpl implements UserService{
    private static final Map<String,String> Users = new HashMap<>();
    @Override
    public boolean userLogin(String name, String password) {
        if(Users.containsKey(name)&&!Users.get(name).equals(password)){
            return false;
        }else{
            Users.put(name,password);
            return true;
        }
    }

    @Override
    public boolean userLogout(String name, String password) {
        if(Users.containsKey(name) && Users.get(name).equals(password)){
            Users.remove(name);
            return true;
        }else{
            return false;
        }
    }
}
