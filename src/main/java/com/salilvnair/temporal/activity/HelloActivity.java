package com.salilvnair.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface HelloActivity {
    @ActivityMethod
    String sayHello(String name);
}