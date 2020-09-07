/*
 * Copyright (c) 2020, TP-Link Co.,Ltd.
 * Author: hujie <hujie1@tp-link.com.cn>
 * Created: 2020-09-01
 */
package com.tplink.hujie.myspringmvc.servlet;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

import com.tplink.hujie.myspringmvc.annotation.*;
import com.tplink.hujie.myspringmvc.aop.AbstractProxyFactory;
import com.tplink.hujie.myspringmvc.aop.ReflectionUtil;

/**
 * @author Hujie
 */
@Component
public class MyHandleMapping {
    private final Properties properties = new Properties();
    private final List<String> classNames = new ArrayList<>();
    private final Map<String, Object> ioc = new HashMap<>();
    private final Map<String, Method> handlerMapping = new  HashMap<>();
    private final Map<String, Object> controllerMap  =new HashMap<>();
    private final Map<String, Object> proxys = new HashMap<>();
    public void init(){
        //1.加载配置文件
        doLoadConfig();
        //2.初始化所有相关联的类,扫描用户设定的包下面所有的类
        doScanner(properties.getProperty("scanPackage"));
        //3.拿到扫描到的类,通过反射机制,实例化,并且放到ioc容器中(k-v  beanName-bean) beanName默认是首字母小写
        doInstance();
        //4.扫描注解,将切面里的切点扫描出来,添加需要代理的类到proxy这个HashMap里
        doAopProxy();
        //5.依赖注入
        doContainerDI();
        //6.初始化HandlerMapping(将url和method对应上)
        initHandlerMapping();
    }

    public Map<String, Method> getHandlerMapping() {
        return handlerMapping;
    }

    public Map<String, Object> getControllerMap() {
        return controllerMap;
    }

    private void doLoadConfig(){
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("application.properties");
        try {
            //用Properties文件加载文件里的内容
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            //关流
            if(null!=resourceAsStream){
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doScanner(String packageName) {
        //把所有的.替换成/
        URL url  =this.getClass().getClassLoader().getResource("/"+packageName.replaceAll("\\.", "/"));
        assert url != null;
        File dir = new File(url.getFile());
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if(file.isDirectory()){
                //读取包
                doScanner(packageName+"."+file.getName());
            }else{
                String className =packageName +"." +file.getName().replace(".class", "");
                classNames.add(className);
            }
        }
    }

    private void doInstance() {
        if (classNames.isEmpty()) {
            return;
        }
        for (String className : classNames) {
            try {
                //反射来实例化
                Class<?> clazz =Class.forName(className);
                if(clazz.isAnnotationPresent(MyController.class)){
                    ioc.put(toLowerFirstWord(clazz.getSimpleName()),clazz.newInstance());
                }else if(clazz.isAnnotationPresent(MyService.class)){
                    ioc.put(toLowerFirstWord(clazz.getSimpleName()),clazz.newInstance());
                    //把接口也放入IOC容器
                    Class[] interfaces = clazz.getInterfaces();
                    for (Class<?> i:interfaces) {
                        ioc.put(toLowerFirstWord(i.getSimpleName()),clazz.newInstance());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void doAopProxy(){
        if (classNames.isEmpty()) {
            return;
        }
        for (String className : classNames){
            try {
                Class<?> clazz = Class.forName(className);
                //判断是否为切面
                if (clazz.isAnnotationPresent(MyAspect.class)){
                    Method[] methods = clazz.getDeclaredMethods();
                    //判断是否为切点
                    for (Method method : methods){
                        if (method.isAnnotationPresent(MyPointCut.class)){
                            MyPointCut xzPointcut = method.getAnnotation(MyPointCut.class);
                            String targetClassName = xzPointcut.classname();
                            String targetMethodName = xzPointcut.methodname();
                            //创建被代理的对象
                            Object target = ReflectionUtil.newInstance(targetClassName);

                            //创建代理工厂
                            AbstractProxyFactory proxyFactory = (AbstractProxyFactory) ReflectionUtil.newInstance(clazz);

                            proxyFactory.setProxyMethodName(targetMethodName);
                            //创建代理对象
                            Object proxy = proxyFactory.createProxy(target);
                            //将原对象的类名和代理对象放入map中
                            if (proxy != null){
                                proxys.put(target.getClass().getSimpleName(), proxy);
                            }
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    private void doContainerDI() {
        if (ioc.isEmpty()){
            return;
        }
        //ioc中的实体
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            //私有字段一样获取
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {

                //如果没有MyAutowired注解
                if (!field.isAnnotationPresent(MyAutowired.class)) {
                    continue;
                }

                //有MyAutowired注解
                MyAutowired autowired = field.getAnnotation(MyAutowired.class);
                String beanName = autowired.value().trim();

                //如果没有别名
                if ("".equals(beanName)) {
                    beanName = field.getType().getSimpleName();
                }
                Object injectInstance = ioc.get(toLowerFirstWord(beanName));

                if (proxys.containsKey(beanName)){
                    injectInstance = proxys.get(beanName);
                }

                //为字段注入实例对象
                try {
                    field.setAccessible(true);
                    field.set(entry.getValue(),injectInstance);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
    }
    private void initHandlerMapping(){
        if(ioc.isEmpty()){
            return;
        }
        try {
            for (Map.Entry<String, Object> entry: ioc.entrySet()) {
                Class<?> clazz = entry.getValue().getClass();
                if(!clazz.isAnnotationPresent(MyController.class)){
                    continue;
                }
                //拼url时,是controller头的url拼上方法上的url
                String baseUrl ="";
                if(clazz.isAnnotationPresent(MyRequestMapping.class)){
                    MyRequestMapping annotation = clazz.getAnnotation(MyRequestMapping.class);
                    baseUrl=annotation.value();
                }
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if(!method.isAnnotationPresent(MyRequestMapping.class)){
                        continue;
                    }
                    MyRequestMapping annotation = method.getAnnotation(MyRequestMapping.class);
                    String url = annotation.value();

                    url =(baseUrl+"/"+url).replaceAll("/+", "/");
                    //这里应该放置实例和method
                    String beanName = toLowerFirstWord(clazz.getSimpleName());
                    handlerMapping.put(url,method);
                    controllerMap.put(url,ioc.get(beanName));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 把字符串的首字母小写
     */
    private String toLowerFirstWord(String name){
        char[] charArray = name.toCharArray();
        charArray[0] += 32;
        return String.valueOf(charArray);
    }

}
