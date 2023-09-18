package com.demo.plugin;

import com.demo.anno.TypeHandler;
import org.apache.ibatis.executor.resultset.DefaultResultSetHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Objects;
import java.util.Properties;

@Intercepts(@Signature(
        type = ResultSetHandler.class,
        method = "handleResultSets",
        args = {Statement.class}
))
public class TypeHandlerPlugin implements Interceptor {

    private static final String FIELD_MAPPED_STATEMENT = "mappedStatement";
    private static final Class<TypeHandler> TYPE_HANDLER_CLASS = TypeHandler.class;

    static {
        System.out.println("plugin init");
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        System.out.println("before");

        PreparedStatement stat = (PreparedStatement) invocation.getArgs()[0];
        System.out.println(stat);
        DefaultResultSetHandler resultSetHandler = (DefaultResultSetHandler) invocation.getTarget();

        // 我们需要的关键信息都保存在 MappedStatement 中，利用反射获取到 MappedStatement
        Class<? extends DefaultResultSetHandler> clazz = resultSetHandler.getClass();
        Field field = clazz.getDeclaredField(FIELD_MAPPED_STATEMENT);
        field.setAccessible(true);
        MappedStatement mappedStatement = (MappedStatement) field.get(resultSetHandler);
        System.out.println(mappedStatement);
        for (ResultMap resultMap : mappedStatement.getResultMaps()) {
            Class<?> pojo = resultMap.getType(); // 获取到 POJO 对象
            // 检查 POJO 对象字段上是否有自定义的 @TypeHandler 注解
            Field[] declaredFields = pojo.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);
                if (Objects.nonNull(declaredField.getAnnotation(TYPE_HANDLER_CLASS))) {
                    // TODO handle annotated field
                }
            }

        }

        Object result = invocation.proceed();
        System.out.println("after");
        return result;
    }

    private void handleAnnotatedFiled() {}

    @Override
    public Object plugin(Object target) {
        return Interceptor.super.plugin(target);
    }

    @Override
    public void setProperties(Properties properties) {
        Interceptor.super.setProperties(properties);
    }
}
