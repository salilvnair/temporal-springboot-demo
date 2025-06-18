package com.salilvnair.temporal.controller;

import com.salilvnair.temporal.model.IntakeRequest;
import com.salilvnair.temporal.type.TaskQueue;
import com.salilvnair.temporal.workflow.IntakeWorkflow;
import com.salilvnair.temporal.workflow.OpsTeamWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/intake")
public class IntakeController {
    private final WorkflowClient workflowClient;

    public IntakeController(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    @PostMapping("/start")
    public String startIntake(@RequestBody IntakeRequest intakeRequest) {
        IntakeWorkflow workflow = workflowClient.newWorkflowStub(
                IntakeWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setWorkflowId("intake-" + intakeRequest.getRequestId())
                        .setTaskQueue(TaskQueue.Name.INTAKE_TASK_QUEUE)
                        .build()
        );
        WorkflowClient.start(workflow::start, intakeRequest);
        return "Started IntakeWorkflow for: " + intakeRequest.getRequestId();
    }

    @PostMapping("/approve")
    public String approveIntake(@RequestBody IntakeRequest intakeRequest) {
        IntakeWorkflow intakeWorkflow = workflowClient.newWorkflowStub(IntakeWorkflow.class, "intake-" + intakeRequest.getRequestId());
        intakeWorkflow.approveInstallation(intakeRequest);
        return "Manager approved intake for: " + intakeRequest.getRequestId();
    }

    @PostMapping("/status")
    public String workflowStatus(@RequestBody IntakeRequest intakeRequest) {
        IntakeWorkflow intakeWorkflow = workflowClient.newWorkflowStub(
                IntakeWorkflow.class, "intake-" + intakeRequest.getRequestId()
        );
        return intakeWorkflow.findCurrentStatus();
    }

    @PostMapping("/cancel-ops")
    public String cancelOpsTeamWorkflow(@RequestBody IntakeRequest intakeRequest) {
        OpsTeamWorkflow workflow = workflowClient.newWorkflowStub(
                OpsTeamWorkflow.class,"ops-" + intakeRequest.getRequestId()
        );

        workflow.cancelWork();  // Signal call
        return "🚨 Cancel signal sent to OpsTeamWorkflow";
    }

}
