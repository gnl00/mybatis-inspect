package com.demo;

import com.demo.service.UserService;

public class Main {
    public static void main(String[] args) {
        UserService user = new UserService();
        user.pay(1, 100, "2023-09-22");
    }
}
