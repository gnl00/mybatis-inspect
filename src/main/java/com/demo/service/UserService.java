package com.demo.service;

public class UserService {

    private int count = 0;
    private int sum = 0;

    public void test() {
        System.out.println("UserService#test");
    }

    public void pay(int amount) {
        System.out.println("UserService#pay(int) ==> " +  amount);
    }

    public void pay(int goodsId, int amount, String time) {
        System.out.println("UserService#pay(int, int) ==> " + goodsId + " ==> " + amount + " ==> " + time);
        sum = amount;
    }

    public int check() {
        return 0;
    }

    @Override
    public String toString() {
        return "UserService{" +
                "count=" + count +
                ", sum=" + sum +
                '}';
    }
}
