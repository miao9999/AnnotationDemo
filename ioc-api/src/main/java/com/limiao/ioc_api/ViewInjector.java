package com.limiao.ioc_api;

import android.app.Activity;
import android.view.View;

/**
 * Created by miao on 2019/3/19.
 * 提供注入的静态方法，间接调用了io-complier的编译生成的类方法
 */
public class ViewInjector {
    public static final String SUFFIX = "$$ViewInject";

    public static void injectView(Activity activity){
        ViewInject proxyActivity = findProxyActivity(activity);
        // 调用 inject 方法，inject 方法的具体实现是在生成的类 MainActivity$$ViewInject 中
        // 在具体的实现里，执行 findViewById
        proxyActivity.inject(activity,activity);
    }

    public static void injectView(Object object, View view){
        ViewInject proxyActivity = findProxyActivity(object);
        proxyActivity.inject(object,view);
    }


    protected static ViewInject findProxyActivity(Object activity){
        try {
            // 获取 要注入的类的类对象
            Class<?> clazz = activity.getClass();
            // 获取 ViewInject 的类对象
            Class<?> injectorClass = Class.forName(clazz.getName() + SUFFIX);
            // 通过类对象创建一个该类的实例
            return (ViewInject) injectorClass.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        throw new RuntimeException(String.format("can not find %s, something when compiler.",activity.getClass().getSimpleName() + SUFFIX));
    }
}
