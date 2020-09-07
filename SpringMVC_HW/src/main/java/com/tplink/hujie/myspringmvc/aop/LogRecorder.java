/*
 * Copyright (c) 2020, TP-Link Co.,Ltd.
 * Author: ShenZhongLi shenzhongli@tp-link.com.cn
 * Created: 2020-09-01
 */
package com.tplink.hujie.myspringmvc.aop;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tplink.hujie.myspringmvc.annotation.MyAspect;
import com.tplink.hujie.myspringmvc.annotation.MyPointCut;


@MyAspect
public class LogRecorder extends AbstractProxyFactory{
    Logger logger= LoggerFactory.getLogger(LogRecorder.class);

    @MyPointCut(classname = "com.tplink.hujie.myspringmvc.service.UserServiceImpl",methodname = "userLogin")
    public void record(){}

    @Override
    public void doBefore() {
        logger.info("登录之前的日志");
    }

    @Override
    public void doAfter() {
        logger.info("登录之后的日志");
    }
}
