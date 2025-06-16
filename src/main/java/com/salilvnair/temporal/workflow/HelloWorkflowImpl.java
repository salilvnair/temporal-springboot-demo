package com.salilvnair.temporal.workflow;

import com.salilvnair.temporal.activity.HelloActivity;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import java.time.Duration;

public class HelloWorkflowImpl implements HelloWorkflow {
    private final HelloActivity activity;

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
            // Simulate Timer before activity call
            Workflow.sleep(Duration.ofSeconds(2));

            String result = activity.sayHello(name);
            System.out.println("✅ Workflow Result: " + result);
        } catch (Exception ex) {
            System.out.println("❌ Activity failed after retries: " + ex.getMessage());
            throw Workflow.wrap(ex); // required to propagate error correctly in Temporal
        }
    }
}