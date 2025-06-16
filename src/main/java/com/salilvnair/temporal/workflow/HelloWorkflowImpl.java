package com.salilvnair.temporal.workflow;

import com.salilvnair.temporal.activity.HelloActivity;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import java.time.Duration;

public class HelloWorkflowImpl implements HelloWorkflow {
    private final HelloActivity activity;

    public HelloWorkflowImpl() {
        ActivityOptions options = ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofSeconds(5))
                .build();
        this.activity = Workflow.newActivityStub(HelloActivity.class, options);
    }

    @Override
    public void run(String name) {
        String result = activity.sayHello(name);
        System.out.println("✅ Workflow Result: " + result);
    }
}