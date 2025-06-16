package com.salilvnair.temporal.activity;

public class HelloActivityImpl implements HelloActivity {
    @Override
    public String sayHello(String name) {
        System.out.println("👋 Trying to greet: " + name);
        int random = (int)(Math.random() * 3) + 1;
        System.out.println("🧮 Radom:"+random);
        if (random == 2) {
            System.out.println("☹️ Simulated failure");
            throw new RuntimeException("Simulated failure in HelloActivity");
        }
        if (random == 3) {
            System.out.println("🙅‍♂️ Don't Retry this");
            throw new IllegalArgumentException("NonRetryableExceptions failure in HelloActivity");
        }
        return "Hello from activity, " + name;
    }
}