package com.demo.aspect;

import org.apache.ibatis.mapping.ResultMapping;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TypeHandlerAspect {

    @Pointcut("execution(* com.demo.utils.*.*(..))")
    public void beforePointcut() {}

    @Pointcut("execution(* org.apache.ibatis.mapping..*.*(..))")
    public void thPointcut() {}

    @Pointcut("execution(* org.springframework.boot.SpringApplication.*(..))")
    public void springPointcut() {}

    @Pointcut("execution(* org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration.sqlSessionFactory(..))")
    public void mybatisPointcut() {}

    @Before("mybatisPointcut()")
    public void testBefore(JoinPoint jp) {
        System.out.println("111 =>> " + jp.getSignature().getName());
        System.out.println("before");
    }

    // @Around("thPointcut()")
    public Object addTypeHandler(ProceedingJoinPoint pjp) throws Throwable {
        ResultMapping resultMapping = (ResultMapping) pjp.getTarget();
        System.out.println(resultMapping.getProperty());
        return pjp.proceed();
    }
}
