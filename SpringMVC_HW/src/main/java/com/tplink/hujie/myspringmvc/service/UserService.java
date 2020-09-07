package com.tplink.hujie.myspringmvc.service;/*
 * Copyright (c) 2020, TP-Link Co.,Ltd.
 * Author: hujie <hujie1@tp-link.com.cn>
 * Created: 2020-09-02
 */

import java.util.HashMap;
import java.util.Map;

public interface UserService {
    /**
     *
     * @param name
     * @param password
     * @return boolean
     */
    public boolean userLogin(String name,String password);

    /**
     *
     * @param name
     * @param password
     * @return boolean
     */
    public boolean userLogout(String name,String password);

}
