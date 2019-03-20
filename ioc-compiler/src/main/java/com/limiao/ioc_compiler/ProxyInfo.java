package com.limiao.ioc_compiler;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

/**
 * Created by miao on 2019/3/19.
 * 根据注解的信息，编写要生成的代码
 */
public class ProxyInfo {
    // 包名
    private String packageName;

    // 要编译生成的类名
    private String proxyClassName;

    // 注解修饰的元素
    private TypeElement mTypeElement;

    // 保存了所有的 bindview 注解的信息
    public Map<Integer,VariableElement> injectVarialbles = new HashMap<>();

     // contentView 的 id
    public int contentViewId;

    public static final String PROXY = "ViewInject";

    public ProxyInfo(Elements elementUtils,TypeElement classElement) {
        this.mTypeElement = classElement;
        // mTypeElement  对应 MainActivity
//        packageElement 对应 MainActivity 的包名元素
        PackageElement packageElement = elementUtils.getPackageOf(mTypeElement);
//        这里的 packageName 也就是 MainActivity 对应的包名
        String packageName = packageElement.getQualifiedName().toString();
        String className = ClassValidator.getClassName(mTypeElement,packageName);


        this.packageName = packageName;
        this.proxyClassName = className + "$$" + PROXY;

        System.out.println("packageName:"+packageName);
        System.out.println("proxyClassName:"+proxyClassName);
        System.out.println("className: "+className);


//        packageName:com.limiao.annotationdemo
//        proxyClassName:MainActivity$$ViewInject
//        className: MainActivity
    }

    /**
     * 生成 java 文件
     * @return
     */
    public String generateJavaCode(){
         StringBuilder stringBuilder = new StringBuilder();
         stringBuilder.append("// Generated code. Do not modify!\n");
         stringBuilder.append("package ").append(packageName).append(";\n\n");
         stringBuilder.append("import com.limiao.*;\n");

         stringBuilder.append("import com.limiao.ioc_api.ViewInject;\n");
         stringBuilder.append('\n');

         stringBuilder.append("public class ").append(proxyClassName)
                 .append(" implements ").append(ProxyInfo.PROXY)
                 .append("<").append(mTypeElement.getQualifiedName())
                 .append(">");
         stringBuilder.append("{\n");

         generateMethods(stringBuilder);

         stringBuilder.append("\n");
         stringBuilder.append("}\n");
         return stringBuilder.toString();
    }

    /**
     * 生成 inject 方法
     * @param builder
     */
    private void generateMethods(StringBuilder builder) {
        builder.append("@Override\n");
        builder.append("public void inject(").append(mTypeElement.getQualifiedName())
                .append(" host, Object source) { \n");


        // if
        builder.append(" if(source instanceof android.app.Activity) {\n");
        // 设置 contentView
        if (contentViewId != 0){
            builder.append("host.setContentView(").append(contentViewId).append(");\n");

        }

        StringBuilder ifStr = new StringBuilder();
        StringBuilder elseStr = new StringBuilder();



        for (int id : injectVarialbles.keySet()) {
            VariableElement variableElement = injectVarialbles.get(id);
            String name = variableElement.getSimpleName().toString();
            String type = variableElement.asType().toString();

            ifStr.append("host.").append(name).append(" = ");
            ifStr.append("(").append(type).append(")(((android.app.Activity)source).findViewById(")
                    .append(id).append("));");

            elseStr.append("host.").append(name).append(" = ");
            elseStr.append("(").append(type).append(")(((android.view.View)source).findViewById(")
                    .append(id).append("));");


        }

        builder.append(ifStr);
        // else
        // 如果是 view 类型，不用设置 contentView
        builder.append("\n}\nelse{\n");
        builder.append(elseStr);
        builder.append("\n}\n");
        builder.append("};");


    }

    public String getProxyClassFullName(){


        return packageName + "." + proxyClassName;
    }

    public TypeElement getTypeElement() {
        return mTypeElement;
    }
}
