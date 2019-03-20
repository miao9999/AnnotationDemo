package com.limiao.ioc_compiler;

import java.lang.annotation.ElementType;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * Created by miao on 2019/3/19.
 * 类的工具
 */
public class ClassValidator {

    /**
     * 判断是否private 修饰
     * @param annotatedClass
     * @return
     */
    static boolean isPrivate(Element annotatedClass){
        return annotatedClass.getModifiers().contains(Modifier.PRIVATE);
    }

    /**
     * 获取类的完整路径
     * @param type
     * @param packgeName
     * @return
     */
    static String getClassName(TypeElement type, String packgeName){
        int packgeLen = packgeName.length() +1;
        return type.getQualifiedName().toString().substring(packgeLen).replace(".","$");
    }
}
