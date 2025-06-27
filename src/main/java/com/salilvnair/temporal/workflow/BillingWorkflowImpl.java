package com.salilvnair.temporal.workflow;

import com.salilvnair.temporal.activity.PostInstallActivity;
import com.salilvnair.temporal.model.IntakeRequest;

public class BillingWorkflowImpl implements BillingWorkflow {
    private final PostInstallActivity activity = PostInstallActivity.newStub();
    @Override
    public void startBilling(IntakeRequest intakeRequest) {
        System.out.println("💰 [BillingWorkflow] Starting...");
        activity.setupBilling(intakeRequest);
        activity.sendInvoice(intakeRequest);
        System.out.println("✅ [BillingWorkflow] Done.");
    }
}
