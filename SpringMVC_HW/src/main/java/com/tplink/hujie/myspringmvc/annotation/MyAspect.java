package com.tplink.hujie.myspringmvc.annotation;/*
 * Copyright (c) 2020, TP-Link Co.,Ltd.
 * Author: hujie <hujie1@tp-link.com.cn>
 * Created: 2020-09-07
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MyAspect {
}