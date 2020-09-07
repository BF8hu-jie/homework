package com.tplink.hujie.myspringmvc.annotation;/*
 * Copyright (c) 2020, TP-Link Co.,Ltd.
 * Author: hujie <hujie1@tp-link.com.cn>
 * Created: 2020-09-05
 */

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.TYPE})
public @interface MyService {
    String value() default "";
}
