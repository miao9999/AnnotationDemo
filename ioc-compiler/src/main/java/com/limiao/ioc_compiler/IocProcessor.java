package com.limiao.ioc_compiler;

import com.google.auto.service.AutoService;
import com.limiao.ioc_annotation.BindView;
import com.limiao.ioc_annotation.ContentView;

import java.awt.image.ConvolveOp;
import java.io.IOException;
import java.io.Writer;
import java.security.Key;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;


/**
 * Created by miao on 2019/3/19.
 * 自定义编、译处理器 ,通过@AutoService 注解在编译时可以自动执行
 */
@AutoService(Processor.class)
public class IocProcessor  extends AbstractProcessor{

    /**
     * 日志打印类
     */
    private Messager mMessager;

    /**
     * 元素工具类
     */
    private Elements mElementUtils;


    /**
     * 保存所有的要注解的文件信息
     */
    private HashMap<String, ProxyInfo> mProxyMap = new HashMap<>();


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mMessager = processingEnvironment.getMessager();
        mElementUtils = processingEnvironment.getElementUtils();

        // 打印 gradle 传进来的参数
        // 这个 map 只是打印了一下，没有在其他地方用到
        Map<String,String> map = processingEnvironment.getOptions();
        // map 是空的 map:{}
        System.out.println("map: "+map.toString());
        for (String s : map.keySet()) {
            mMessager.printMessage(Diagnostic.Kind.NOTE,"key:" + map.get(s));
        }

    }

    /**
     * 此方法用来设置支持的注解类型，没有设置的无效（获取不到）
     * */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> supports = new LinkedHashSet<>();
        supports.add(BindView.class.getCanonicalName());
        supports.add(ContentView.class.getCanonicalName());
        return supports;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        System.out.println("SourceVersion.latestSupported(): "+SourceVersion.latestSupported());
//      版本：要这个值有什么用  SourceVersion.latestSupported(): RELEASE_8
        return SourceVersion.latestSupported();
    }

    /**
     *
     * @param set  包含的是所有使用的[注解的信息]，例如BindView，ContentView
     * @param roundEnvironment 返回的是所有被注解的[元素]，例如类，属性等
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
//        mMessager.printMessage(Diagnostic.Kind.NOTE,"process...");

        // 防止处理多次，要清空
        mProxyMap.clear();

        System.out.println("mProxyMap"+mProxyMap.toString());
        // 获取全部的带有 bindview 注解的 element
        Set<? extends Element> elseWithBind = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        System.out.println("elseWithBind: " + elseWithBind.toString());
        //elseWithBind: [mTextView, mImageView]

        // 对bindview 进行循环，构建 proxyInfo 信息
        for (Element element : elseWithBind) {
            // 检查 element 的合法性
            checkSAnnotationValid(element,BindView.class);

            // 强转成属性元素
            VariableElement variableElement = (VariableElement) element;
            System.out.println("variableElement: "+variableElement);
//            variableElement: mTextView

            System.out.println("-----"+ element.getEnclosingElement());

            // 要获取类元素的类名，直接用 element 也可以，强转不是必须的。
            // 属性元素的外层一定是类元素
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
            System.out.println("typeElement: "+typeElement);
            // typeElement: com.limiao.annotationdemo.MainActivity

            // 获取类元素的类名（全路径名）
            String fqClassName = typeElement.getQualifiedName().toString();
            System.out.println("fqClassName: "+fqClassName);
//            fqClassName: com.limiao.annotationdemo.MainActivity

            System.out.println("mProxyMap: "+mProxyMap);

            ProxyInfo proxyInfo = mProxyMap.get(fqClassName);
            if (proxyInfo == null){
                proxyInfo = new ProxyInfo(mElementUtils,typeElement);
                // 以 class 名称为 key，保存到 mProxy 中
                mProxyMap.put(fqClassName,proxyInfo);
            }

            System.out.println("proxyInfo:"+proxyInfo);

            // 获取 bindview 注解，把信息放入 proxyInfo 中
            BindView bindAnnotation = element.getAnnotation(BindView.class);
            int id = bindAnnotation.value();
            mMessager.printMessage(Diagnostic.Kind.NOTE,"proxyInfo:" + proxyInfo.toString());
            mMessager.printMessage(Diagnostic.Kind.NOTE,"variableElement:" + variableElement);
            mMessager.printMessage(Diagnostic.Kind.NOTE,"id:" + id);
            // 上面的强转要用到这里，作为参数
            proxyInfo.injectVarialbles.put(id,variableElement);
        }

        // 获取所有的 ContentView 注解，操作原理和上面的 bindview 一样
        Set<? extends Element> contentAnnotations = roundEnvironment.getElementsAnnotatedWith(ContentView.class);
        for (Element element : contentAnnotations) {
            TypeElement typeElement = (TypeElement) element;
            String fqClassName  = typeElement.getQualifiedName().toString();
            ProxyInfo proxyInfo = mProxyMap.get(fqClassName);
            if (proxyInfo == null) {
                proxyInfo = new ProxyInfo(mElementUtils,typeElement);
                mProxyMap.put(fqClassName,proxyInfo);
            }
            ContentView contentViewAnnotation = element.getAnnotation(ContentView.class);
            proxyInfo.contentViewId = contentViewAnnotation.value();
        }

        // 循环生成源文件
        for (String key : mProxyMap.keySet()) {
            ProxyInfo proxyInfo = mProxyMap.get(key);
            try {
                System.out.println("ProxyClassFullName: "+proxyInfo.getProxyClassFullName());
                System.out.println("TypeElement: "+proxyInfo.getTypeElement());
                // 创建一个 javaFile 文件
                // 第一个参数：创建的文件名，包含全路径
                // 第二个参数：与此文件的创建有因果关联的类型或包或模块元素，可以为null（文档的说明）
//                JavaFileObject jfo = processingEnv.getFiler().createSourceFile(proxyInfo.getProxyClassFullName(),proxyInfo.getTypeElement());
//                试了一下，如果第二个参数为 null ，也没有关系，还能正常编译运行
                JavaFileObject jfo = processingEnv.getFiler().createSourceFile(proxyInfo.getProxyClassFullName(),null);
                Writer writer = jfo.openWriter();
                writer.write(proxyInfo.generateJavaCode());
                writer.flush();
                writer.close();
            } catch (IOException e) {

                e.printStackTrace();
                error(proxyInfo.getTypeElement(),"unable to write injector for type %s: %s",proxyInfo.getTypeElement(),e.getMessage());
            }
        }

        return true;

    }

    /**
     * 检查BindView修饰的元素的合法性
     * @param element 被注解的元素
     * @param clazz 注解类
     * @return
     */
    private boolean checkSAnnotationValid(Element element, Class<?> clazz) {
        if (element.getKind() != ElementKind.FIELD) {
            error(element, "%s must be delared on field.", clazz.getSimpleName());
            return false;
        }
        if (ClassValidator.isPrivate(element)) {
            error(element, "%s() must can not be private.", element.getSimpleName());
            return false;
        }
        return true;
    }

    /**
     * 打印错误日志方法
     * */
    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        mMessager.printMessage(Diagnostic.Kind.NOTE, message, element);
    }
}
