package com.salilvnair.temporal.workflow;

import com.salilvnair.temporal.activity.PostInstallActivity;
import com.salilvnair.temporal.model.IntakeRequest;

public class SupportSetupWorkflowImpl implements SupportSetupWorkflow {
    private final PostInstallActivity activity = PostInstallActivity.newStub();

    @Override
    public void startSupportSetup(IntakeRequest intakeRequest) {
        System.out.println("🛎️ [SupportSetupWorkflow] Starting...");
        activity.registerSupportProfile(intakeRequest);
        activity.sendWelcomeKit(intakeRequest);
        System.out.println("✅ [SupportSetupWorkflow] Done.");
    }
}
