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
        boolean bandwidthOk = siteCheck.checkBandwidth(intakeRequest.getZip(), intakeRequest.getMbps());

        if (!serviceAvailable || !addressValid || !bandwidthOk) {
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
