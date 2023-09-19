package com.demo.plugin;

import com.demo.anno.TypeHandler;
import org.apache.ibatis.executor.resultset.DefaultResultSetHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

@Intercepts(@Signature(
        type = ResultSetHandler.class,
        method = "handleResultSets",
        args = {Statement.class}
))
public class TypeHandlerPlugin implements Interceptor {

    private static final String MAPPED_STATEMENT_FIELD = "mappedStatement";
    private static final String TYPE_HANDLER_FIELD = "typeHandler";
    private static final Class<TypeHandler> TYPE_HANDLER_CLASS = TypeHandler.class;

    @Value("${mb.type-handler-pkg}")
    private String typeHandlerPackage;

    static {
        System.out.println("plugin init");
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        System.out.println("before");

        PreparedStatement stat = (PreparedStatement) invocation.getArgs()[0];
        System.out.println(stat);

        DefaultResultSetHandler resultSetHandler = (DefaultResultSetHandler) invocation.getTarget();
        MappedStatement mappedStatement = getMappedStatement(resultSetHandler);
        handleMappedStatement(mappedStatement);

        Object result = invocation.proceed();

        System.out.println("after");
        return result;
    }

    private MappedStatement getMappedStatement(DefaultResultSetHandler resultSetHandler) throws NoSuchFieldException, IllegalAccessException {
        if (Objects.isNull(resultSetHandler)) return null;
        // 我们需要的关键信息都保存在 MappedStatement 中，利用反射获取到 MappedStatement
        Class<? extends DefaultResultSetHandler> clazz = resultSetHandler.getClass();
        Field field = clazz.getDeclaredField(MAPPED_STATEMENT_FIELD);
        field.setAccessible(true);
        MappedStatement mappedStatement = (MappedStatement) field.get(resultSetHandler);

        System.out.println(mappedStatement);

        return mappedStatement;
    }

    private void handleMappedStatement(MappedStatement ms) {
        if (Objects.isNull(ms)) return;

        List<ResultMap> resultMaps = null;
        if (!CollectionUtils.isEmpty(resultMaps = ms.getResultMaps())) {
            resultMaps.stream()
                    .filter(rm -> rm.getType().getPackage().getName().equals(typeHandlerPackage)) // 获取 typeHandlerPackage 包下的所有类
                    .forEach(rm -> handleResultMap(rm));
        }
    }

    private void handleResultMap(ResultMap rm) {
        Class<?> type = rm.getType();
        Field[] declaredFields = type.getDeclaredFields();
        Map<String, TypeHandler> fieldAnnotationMap = handleAnnotatedFiled(declaredFields);

        // 获取到定义在 XML 中的 ResultMap
        List<ResultMapping> resultMappings = rm.getResultMappings();
        resultMappings.forEach(rms -> {
            TypeHandler anno = fieldAnnotationMap.get(rms.getProperty());
            Class<? extends ResultMapping> rmsClass = rms.getClass();
            try {
                Field typeHandler = rmsClass.getDeclaredField(TYPE_HANDLER_FIELD);
                typeHandler.setAccessible(true);
                typeHandler.set(rmsClass, anno.value());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Map<String, TypeHandler> handleAnnotatedFiled(Field[] declaredFields) {
        Map<String, TypeHandler> fieldAnnotationMap = new HashMap<>();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            TypeHandler anno = null;
            // 检查 POJO 对象属性上是否有自定义的 @TypeHandler 注解
            if (Objects.nonNull(anno = field.getAnnotation(TYPE_HANDLER_CLASS))) {
                fieldAnnotationMap.put(field.getName(), anno);
            }
        }
        return fieldAnnotationMap;
    }

    @Override
    public Object plugin(Object target) {
        return Interceptor.super.plugin(target);
    }

    @Override
    public void setProperties(Properties properties) {
        Interceptor.super.setProperties(properties);
    }
}
