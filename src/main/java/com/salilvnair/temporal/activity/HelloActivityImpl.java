package com.salilvnair.temporal.activity;

public class HelloActivityImpl implements HelloActivity {
    @Override
    public String sayHello(String name) {
        return "👋 Hello from activity, " + name;
    }
}