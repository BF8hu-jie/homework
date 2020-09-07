/*
 * Copyright (c) 2020, TP-Link Co.,Ltd.
 * Author: hujie <hujie1@tp-link.com.cn>
 * Created: 2020-09-01
 */
package com.tplink.hujie.myspringmvc.servlet;


import org.apache.commons.io.FileUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.tplink.hujie.myspringmvc.annotation.MyRequestParam;

public class MyDispatcherServlet extends HttpServlet{

    private final MyHandleMapping myHandleMapping = new MyHandleMapping();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        System.out.println("=====MyDispatcherServlet init===========");
        myHandleMapping.init();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, Method> handlerMapping =  myHandleMapping.getHandlerMapping();
        Map<String, Object> controllerMap  = myHandleMapping.getControllerMap();

        if(handlerMapping.isEmpty()){
            return;
        }
        String url =req.getRequestURI();
        String contextPath = req.getContextPath();

        //拼接url并把多个/替换成一个
        url=url.replace(contextPath, "").replaceAll("/+", "/");
        if(!handlerMapping.containsKey(url)){
            resp.getWriter().println("404 NOT FOUND!!!");
            return;
        }

        Method method =handlerMapping.get(url);

        //获取方法的参数列表
        Class<?>[] parameterTypes = method.getParameterTypes();

        //获取请求的参数
        Map<String, String[]> parameterMap = req.getParameterMap();

        //获取方法中加了注解的参数
        Map<String,Integer> paramIndexMap = putParamIndexMapping(method);

        //保存参数值
        Object [] paramValues= new Object[parameterTypes.length];

        //方法的参数列表
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String value = Arrays.toString(entry.getValue()).replaceAll("\\[|\\]", "").replaceAll("/+", "/");
            //如果找到了匹配的值，就填充
            if (!paramIndexMap.containsKey(entry.getKey())) {
                continue;
            }
            Integer index = paramIndexMap.get(entry.getKey());
            paramValues[index] = convert(parameterTypes[index], value);
        }
        //设置方法中的request对象和response对象
        Integer reqIndex = paramIndexMap.get(HttpServletRequest.class.getName());
        Integer respIndex = paramIndexMap.get(HttpServletResponse.class.getName());
        paramValues[reqIndex] = req;
        paramValues[respIndex] = resp;

        //利用反射机制来调用
        try {
            String res = (String)method.invoke(controllerMap.get(url), paramValues);
            resp.getWriter().println(res);
        }catch (Exception e){

        }

    }

    private Object convert(Class<?> parameterType, String value) {
        if (parameterType == Integer.class) {
            return Integer.valueOf(value);
        }
        return value;
    }

    private Map<String,Integer> putParamIndexMapping(Method method) {
        //获取方法中加了注解的参数
        Map<String,Integer> paramIndexMap=new HashMap<>(10);
        Annotation[][] annotations = method.getParameterAnnotations();
        for (int i =0; i < annotations.length;i++){
            for (Annotation annotation : annotations[i]){
                if (annotation instanceof MyRequestParam){
                    String paramName = ((MyRequestParam) annotation).value();
                    paramIndexMap.put(paramName,i);
                }
            }
        }
        //获取方法中的request和response的参数
        Class<?>[] paramTypes = method.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++){
            Class<?> paramType = paramTypes[i];
            if (paramType == HttpServletRequest.class || paramType == HttpServletResponse.class){
                paramIndexMap.put(paramType.getName(),i);
            }
        }
        return paramIndexMap;

    }

}
