package com.salilvnair.temporal.activity;

import com.salilvnair.temporal.model.IntakeRequest;
import com.salilvnair.temporal.type.TaskQueue;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

@ActivityInterface
public interface PostInstallActivity {
    @ActivityMethod
    void provisionInternet(IntakeRequest request);

    @ActivityMethod
    void provisionVoIP(IntakeRequest request);

    @ActivityMethod
    void setupBilling(IntakeRequest request);

    @ActivityMethod
    void sendInvoice(IntakeRequest request);

    @ActivityMethod
    void registerSupportProfile(IntakeRequest request);

    @ActivityMethod
    void sendWelcomeKit(IntakeRequest request);


     static PostInstallActivity newStub() {
        return Workflow.newActivityStub(
                PostInstallActivity.class,
                ActivityOptions.newBuilder()
                        .setStartToCloseTimeout(Duration.ofSeconds(10))
                        .setTaskQueue(TaskQueue.Name.POST_INSTALL_TASK_QUEUE)
                        .build()
        );
    }
}
