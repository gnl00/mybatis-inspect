package com.demo.aspect;

import com.demo.anno.TypeHandler;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

@Aspect
@Component
public class MyBatisAspect {

    private static final String TYPE_HANDLER_COLUMN = "typeHandler";

    private String typeHandlerPkg = "com.demo.entity";

    @Value("${mb.type-handler-pkg}")
    private String injectedTypeHandlerPkg;

    private TypeAliasRegistry typeAliasRegistry;
    private Class<?> currentClass;

    @Pointcut("execution(* org.mybatis.spring.SqlSessionFactoryBean.buildSqlSessionFactory(..))")
    public void buildSqlSessionFactory() {}

    // org.apache.ibatis.builder.MapperBuilderAssistant.buildResultMapping(java.lang.Class<?>, java.lang.String, java.lang.String, java.lang.Class<?>, org.apache.ibatis.type.JdbcType, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Class<? extends org.apache.ibatis.type.TypeHandler<?>>, java.util.List<org.apache.ibatis.mapping.ResultFlag>)
    @Pointcut("execution(* org.apache.ibatis.builder.MapperBuilderAssistant.buildResultMapping(..))")
    public void buildResultMapping() {}

    @Pointcut("execution(* org.apache.ibatis.builder.xml.XMLMapperBuilder.resultMapElement(*))")
    public void resultMapElement() {}

    @Pointcut("execution(* org.apache.ibatis.parsing.XNode.getChildren())")
    public void getChildren() {}

    @Before("buildSqlSessionFactory()")
    public void buildSqlSessionFactory_before() {
        System.out.println("buildSqlSessionFactory ##> before");
    }

    @Before("resultMapElement()")
    public void resultMapElement_before(JoinPoint jp) {
        System.out.println("resultMapElement ##> before");
        XNode resultMapNode = (XNode) jp.getArgs()[0];
        String aliasType = resultMapNode.getStringAttribute("type");
        XMLMapperBuilder mapperBuilder = (XMLMapperBuilder) jp.getThis();
        typeAliasRegistry = mapperBuilder.getConfiguration().getTypeAliasRegistry();
        currentClass = typeAliasRegistry.resolveAlias(aliasType);
    }

    /**
     * getChildrenAround 需要配合 resultMapElement_before 一起使用
     * 因为在执行 org.apache.ibatis.parsing.XNode.getChildren 方法的时候无法获取到需要处理的实体类全限定类名，
     * 所以在 org.apache.ibatis.builder.xml.XMLMapperBuilder.resultMapElement 方法执行前将全限定类名缓存起来，
     * 调用 getChildren 方法的时候获取
     */
    // @Around(value = "getChildren()")
    public Object getChildrenAround(ProceedingJoinPoint pjp) throws Throwable {
        List<XNode> xNodes = (List<XNode>) pjp.proceed();
        if (!typeHandlerPkg.equals(currentClass.getPackageName()) || xNodes.isEmpty()) return xNodes;

        System.out.println("xNodes not empty");
        xNodes.stream().filter(node -> node.getName().equals("result"))
                .forEach(node -> {
                    System.out.println("handle result node");
                    Class<? extends XNode> nodeClass = node.getClass();
                    try {
                        Field attrField = nodeClass.getDeclaredField("attributes");
                        attrField.setAccessible(true);
                        System.out.println("got attributes field");
                        Properties attributes = (Properties) attrField.get(node);
                        String nodeProp = attributes.getProperty("property");
                        System.out.println("got target column from attributes ##> property");
                        System.out.println("current class ##> " + currentClass);
                        System.out.println("convert xml node property to java field");
                        Field javaField = currentClass.getDeclaredField(nodeProp);
                        javaField.setAccessible(true);
                        TypeHandler anno = null;
                        System.out.println("list all annotations on java field ##> " + javaField.getName());
                        for (Annotation an : javaField.getAnnotations()) {
                            System.out.println(an);
                        }
                        System.out.println("check target annotation on java field ##> " + javaField.getName());
                        if(Objects.nonNull((anno = javaField.getAnnotation(TypeHandler.class)))) {
                            System.out.println("found annotation " + anno.value().getName());
                            System.out.println("found annotation " + anno.value().getTypeName());
                            attributes.setProperty(TYPE_HANDLER_COLUMN, anno.value().getName());
                        }
                        // if (!attributes.contains("typeHandler")) {}
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
        System.out.println("getChildrenAround ##> " + xNodes);

        return xNodes;
    }

    /**
     * org.apache.ibatis.builder.MapperBuilderAssistant.buildResultMapping 方法的执行时机晚于 org.apache.ibatis.builder.xml.XMLMapperBuilder.resultMapElement
     * 可以在 buildResultMapping 方法中获取到所有需要的信息，但是在处理方法参数的时候只能根据 TypeHandler 下标来处理
     */
    @Around("buildResultMapping()")
    public Object buildResultMapping_around(ProceedingJoinPoint pjp) {
        System.out.println("buildResultMapping ##> around");
        System.out.println(" typeHandler from yaml ##> " + injectedTypeHandlerPkg);

        try {
            Object[] args = pjp.getArgs();
            System.out.println("pre process args ##> " + Arrays.stream(args).toList());
            Class<?> entity = (Class<?>) args[0];

            // checking package
            String currentPkgName = entity.getPackageName();
            System.out.println("current package ##> " + currentPkgName);
            if (!currentPkgName.equals(typeHandlerPkg)) {
                System.out.println("no need to handle this package ##> " + currentPkgName);
                return pjp.proceed(args);
            }

            String xmlProperty = (String) args[1];
            System.out.println("handle xml property ##> " + xmlProperty);

            Field javaField = entity.getDeclaredField(xmlProperty);
            System.out.println("got java field ##> " + javaField.getName());
            javaField.setAccessible(true);
            TypeHandler targetAnno = javaField.getAnnotation(TypeHandler.class);

            // check if the xmlProperty has a target annotation
            if (Objects.isNull(targetAnno)) {
                System.out.println("no target annotation on field ##> " + javaField);
                return pjp.proceed(args);
            }
            System.out.println("got target annotation from java field ##> " + targetAnno);

            // set typeHandler value
            if (Objects.isNull(args[9])) {
                System.out.println("set typeHandler");
                args[9] = targetAnno.value();
            }
            System.out.println("post process args ##> " + Arrays.stream(args).toList());
            return pjp.proceed(args);
        } catch (NoSuchFieldException e) {
            System.out.println("buildResultMapping_around raise NoSuchFieldException " + e.getMessage());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
