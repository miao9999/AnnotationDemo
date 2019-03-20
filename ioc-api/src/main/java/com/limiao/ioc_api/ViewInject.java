package com.limiao.ioc_api;

/**
 * Created by miao on 2019/3/19.
 * 注入接口
 */
public interface ViewInject<T> {
    void inject(T target,Object source);
}
