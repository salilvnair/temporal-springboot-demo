package com.salilvnair.temporal.workflow;

import com.salilvnair.temporal.activity.SiteCheckActivity;
import com.salilvnair.temporal.model.IntakeRequest;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class IntakeWorkflowImpl implements IntakeWorkflow {
    private boolean approved = false;

    private final SiteCheckActivity siteCheck = Workflow.newActivityStub(
            SiteCheckActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .build()
    );

    @Override
    public void start(IntakeRequest intakeRequest) {
        System.out.println("📥 Intake received for: "+intakeRequest.getRequestId());
        boolean serviceAvailable = siteCheck.checkZipService(intakeRequest.getZip());
        boolean addressValid = siteCheck.validateAddress(intakeRequest.getAddress());
//        boolean addressValid1 = siteCheck.validateAddress1(intakeRequest.getAddress());
//        ❌ io.temporal.worker.NonDeterministicException:
//        [TMPRL1100] COMMAND_TYPE_SCHEDULE_ACTIVITY_TASK doesn't match EVENT_TYPE_ACTIVITY_TASK_SCHEDULED...
//        🚨 Why You're Getting This Error
//
//        Temporal records every step of the workflow into its event history — including activity method names, parameters, and order.
//
//        You changed the order or added a new activity:
//
////    previously:
//        validateAddress();      // Event ID 16
//        checkBandwidth();       // Event ID 17
//
////    now (you added in between):
//        validateAddress();      // Event ID 16
//        validateAddress1();     // NEW — not recorded before
//        checkBandwidth();       // Now this becomes Event ID 18
        boolean addressValid1 = true;
        int version = Workflow.getVersion("validateAddress1", Workflow.DEFAULT_VERSION, 1);
        if (version == 1) {
            addressValid1 = siteCheck.validateAddress1(intakeRequest.getAddress());
            if (!addressValid1) {
                System.out.println("❌ Address validation 1 failed");
                return;
            }
        }

        boolean bandwidthOk = siteCheck.checkBandwidth(intakeRequest.getZip(), intakeRequest.getMbps());

        if (!serviceAvailable || !addressValid || !addressValid1 || !bandwidthOk) {
            System.out.println("❌ Pre-checks failed");
            return;
        }

        System.out.println("✅ All checks passed. 📋 Waiting for manager approval...");

        // Wait for approval signal
        Workflow.await(() -> approved);

        System.out.println("✅ Manager approved. Triggering InstallationWorkflow...");

        // Trigger child workflow
        InstallationWorkflow child = Workflow.newChildWorkflowStub(InstallationWorkflow.class);
        child.startInstallation(intakeRequest);
    }

    @Override
    public void approveInstallation(IntakeRequest intakeRequest) {
        System.out.println("📩 Received manager approval signal!:"+intakeRequest.getRequestId());
        this.approved = true;
    }
}
