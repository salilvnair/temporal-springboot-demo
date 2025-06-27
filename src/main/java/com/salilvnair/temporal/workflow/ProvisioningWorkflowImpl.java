package com.salilvnair.temporal.workflow;

import com.salilvnair.temporal.activity.PostInstallActivity;
import com.salilvnair.temporal.model.IntakeRequest;

public class ProvisioningWorkflowImpl implements ProvisioningWorkflow {
    private final PostInstallActivity activity = PostInstallActivity.newStub();
    @Override
    public void startProvisioning(IntakeRequest intakeRequest) {
        System.out.println("📦 [ProvisioningWorkflow] Starting...");
        activity.provisionInternet(intakeRequest);
        activity.provisionVoIP(intakeRequest);
        System.out.println("✅ [ProvisioningWorkflow] Done.");
    }
}
