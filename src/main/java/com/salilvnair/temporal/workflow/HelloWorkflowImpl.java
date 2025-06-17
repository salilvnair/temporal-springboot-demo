package com.salilvnair.temporal.workflow;

import com.salilvnair.temporal.activity.HelloActivity;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import java.time.Duration;

public class HelloWorkflowImpl implements HelloWorkflow {
    private final HelloActivity activity;
    private String greeting = "Default"; // 👈 state to query

    public HelloWorkflowImpl() {
        RetryOptions retryOptions = RetryOptions.newBuilder()
                                .setInitialInterval(Duration.ofSeconds(1))
                                .setBackoffCoefficient(2.0)
                                .setMaximumAttempts(3)
                                .setDoNotRetry(IllegalArgumentException.class.getName())
                                .build();
        ActivityOptions options = ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofSeconds(3))
                .setRetryOptions(retryOptions)
                .build();

        this.activity = Workflow.newActivityStub(HelloActivity.class, options);
    }

    @Override
    public void run(String name) {
        try {
            greeting = activity.sayHello(name);
            System.out.println("✅ Initial Greeting: " + greeting);

            // Keep workflow alive to accept signals
            Workflow.await(() -> false); // Keeps the workflow running forever (until terminated externally)
        } catch (Exception ex) {
            System.out.println("❌ Activity failed after retries: " + ex.getMessage());
            throw Workflow.wrap(ex); // required to propagate error correctly in Temporal
        }
    }

    @Override
    public void updateGreeting(String newMessage) {
        System.out.println("🔔 Received signal: " + newMessage);
        greeting = newMessage;
    }

    @Override
    public String getGreeting() {
        return greeting;
    }
}