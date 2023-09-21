package com.demo.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import java.util.Arrays;

@Aspect
public class UserServiceAspect {

    @Pointcut("execution(* com.demo.service.UserService.test(..))")
    public void test() {}

    @Pointcut("execution(* com.demo.service.UserService.pay(..)) && args(goodsId, ..)")
    public void pay(int goodsId) {
    }

    @Pointcut("execution(* com.demo.service.UserService.check())")
    public void check() {}

    @Before("test()")
    public void testBefore() {
        System.out.println("before");
    }

    @After("test()")
    public void testAfter() {
        System.out.println("after");
    }

    @Before("pay(goodsId)")
    public void payBefore(JoinPoint jp, int goodsId) {
        System.out.println(goodsId);
        System.out.println("UserServiceAspect ##> payBefore ##> " + Arrays.stream(jp.getArgs()).toList());
    }

    @Around(value = "check()")
    public Object checkAfterReturning(ProceedingJoinPoint pjp) throws Throwable {
        int result = (int) pjp.proceed();
        System.out.println("catch result: " + result);
        result = 200;
        System.out.println("result changed");
        return result;
    }
}
