package com.demo.controller;

import com.demo.utils.MyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private MyUtils myUtils;

    @Autowired
    private ApplicationContext ac;

    @GetMapping("/test")
    public void test() {
        myUtils.str();

        for (String beanDefinitionName : ac.getBeanDefinitionNames()) {
            System.out.println(beanDefinitionName);
        }
    }

}
