package com.salilvnair.temporal.activity;

public class HelloActivityImpl implements HelloActivity {
    @Override
    public String sayHello(String name) {
        System.out.println("👋 Trying to greet: " + name);

        if (Math.random() < 0.7) {
            System.out.println("💥 Simulated failure");
            throw new RuntimeException("Simulated failure in HelloActivity");
        }

        return "Hello from activity, " + name;
    }
}