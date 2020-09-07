package com.tplink.hujie.myspringmvc.annotation;/*
 * Copyright (c) 2020, TP-Link Co.,Ltd.
 * Author: hujie <hujie1@tp-link.com.cn>
 * Created: 2020-09-01
 */

import java.lang.annotation.*;

/**
 * @author Hujie
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyController {
    /**
     * 表示给controller注册别名
     * @return
     */
    String value() default "";

}