package com.salilvnair.temporal.workflow;

import com.salilvnair.temporal.activity.IntakeActivity;
import com.salilvnair.temporal.model.IntakeRequest;
import com.salilvnair.temporal.type.TaskQueue;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.ChildWorkflowOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class IntakeWorkflowImpl implements IntakeWorkflow {
    private boolean approved = false;
    private String status = "Not started";

    private final IntakeActivity intakeActivity = Workflow.newActivityStub(
            IntakeActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .build()
    );

    @Override
    public void start(IntakeRequest intakeRequest) {
        status = "🏃‍♂️Running pre-checks...";
        System.out.println("📥 Intake received for: "+intakeRequest.getRequestId());
        boolean serviceAvailable = intakeActivity.checkZipService(intakeRequest.getZip());
        boolean addressValid = intakeActivity.validateAddress(intakeRequest.getAddress());
        boolean metroAddress;
        int version = Workflow.getVersion("validateMetroAddress", Workflow.DEFAULT_VERSION, 1);
        if (version == 1) {
            metroAddress = intakeActivity.validateMetroAddress(intakeRequest.getAddress());
            if (!metroAddress) {
                status = "Pre-checks failed ❌";
                System.out.println("❌ Metro address validation  failed");
                return;
            }
        }

        boolean bandwidthOk = intakeActivity.checkBandwidth(intakeRequest.getZip(), intakeRequest.getMbps());

        if (!serviceAvailable || !addressValid || !bandwidthOk) {
            status = "Pre-checks failed ❌";
            System.out.println("❌ Pre-checks failed");
            return;
        }

        System.out.println("✅ All checks passed. 📋 Waiting for manager approval...");

        // Wait for approval signal
        status = "Waiting for manager approval...👮‍♂️⏱️";
        Workflow.await(() -> approved);
        status = "👮‍♂️✅ Manager approved. Triggering InstallationWorkflow...";
        System.out.println("✅ Manager approved. Triggering InstallationWorkflow...");

        // Trigger child workflow
        InstallationWorkflow installationWorkflow = Workflow.newChildWorkflowStub(InstallationWorkflow.class);
        installationWorkflow.startInstallation(intakeRequest);
        status = "Installation complete 🚀";

        ChildWorkflowOptions childOptions = ChildWorkflowOptions.newBuilder()
                                                .setTaskQueue(TaskQueue.Name.POST_INSTALL_TASK_QUEUE)
                                                .setWorkflowRunTimeout(Duration.ofMinutes(10))
                                                .build();

        ProvisioningWorkflow provisioning = Workflow.newChildWorkflowStub(ProvisioningWorkflow.class, childOptions);
        provisioning.startProvisioning(intakeRequest);

        BillingWorkflow billing = Workflow.newChildWorkflowStub(BillingWorkflow.class, childOptions);
        billing.startBilling(intakeRequest);

        SupportSetupWorkflow support = Workflow.newChildWorkflowStub(SupportSetupWorkflow.class, childOptions);
        support.startSupportSetup(intakeRequest);

        status = "Post-install workflows completed 🧩";
        System.out.println("✅ All post-install workflows executed");
    }

    @Override
    public void approveInstallation(IntakeRequest intakeRequest) {
        System.out.println("📩 Received manager approval email:"+intakeRequest.getRequestId());
        status = "📩 Received manager approval email";
        this.approved = true;
    }

    @Override
    public String findCurrentStatus() {
        return status;
    }
}
