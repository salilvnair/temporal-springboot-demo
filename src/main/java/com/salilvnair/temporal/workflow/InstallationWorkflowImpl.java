package com.salilvnair.temporal.workflow;

import com.salilvnair.temporal.model.IntakeRequest;
import com.salilvnair.temporal.type.TaskQueue;
import io.temporal.workflow.ChildWorkflowOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class InstallationWorkflowImpl implements InstallationWorkflow {
    @Override
    public void startInstallation(IntakeRequest intakeRequest) {
        String zipCode = intakeRequest.getZip();
        String address = intakeRequest.getAddress();
        System.out.println("🚀 [InstallationWorkflow] Starting for ZIP: " + zipCode + ", Address: " + address);
        ChildWorkflowOptions childOptions = ChildWorkflowOptions.newBuilder()
                                            .setWorkflowId("ops-" + Workflow.getInfo().getWorkflowId())
                                            .setTaskQueue(TaskQueue.Name.OPS_TASK_QUEUE)
                                            .setWorkflowRunTimeout(Duration.ofMinutes(10))
                                            .build();

        OpsTeamWorkflow ops = Workflow.newChildWorkflowStub(OpsTeamWorkflow.class, childOptions);

        // Simulate steps
        ops.installService(intakeRequest);

        System.out.println("✅ [InstallationWorkflow] Installation steps completed.");
    }
}