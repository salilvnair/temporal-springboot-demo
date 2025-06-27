package com.salilvnair.temporal.controller;

import com.salilvnair.temporal.entity.WorkflowDbEntity;
import com.salilvnair.temporal.model.IntakeRequest;
import com.salilvnair.temporal.model.IntakeResponse;
import com.salilvnair.temporal.model.WorkflowRequest;
import com.salilvnair.temporal.service.WorkflowDbService;
import com.salilvnair.temporal.type.TaskQueue;
import com.salilvnair.temporal.workflow.IntakeWorkflow;
import com.salilvnair.temporal.workflow.OpsTeamWorkflow;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private WorkflowDbService workflowDbService;

    @PostMapping("/start")
    public IntakeResponse start(@RequestBody IntakeRequest intakeRequest) {
        IntakeWorkflow workflow = workflowClient.newWorkflowStub(
                IntakeWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TaskQueue.Name.INTAKE_TASK_QUEUE)
                        .build()
        );
        WorkflowExecution workflowExecution = WorkflowClient.start(workflow::start, intakeRequest);
        String workflowId = workflowExecution.getWorkflowId();
        System.out.println("Started IntakeWorkflow for: " + workflowId);
        WorkflowDbEntity workflowDbEntity = workflowDbService.save(intakeRequest, workflowExecution);
        return IntakeResponse
                    .builder()
                    .dbId(workflowDbEntity.getDbId())
                    .build();
    }

    @PostMapping("/approve")
    public String approveIntake(@RequestBody WorkflowRequest workflowRequest) {
        WorkflowDbEntity dbEntity = workflowDbService.findWorkflowId(workflowRequest.getDbId());
        IntakeWorkflow intakeWorkflow = workflowClient.newWorkflowStub(IntakeWorkflow.class, dbEntity.getWorkflowId());
        intakeWorkflow.approveInstallation(dbEntity.getIntakeRequest());
        return "Manager approved intake for: " + dbEntity.getIntakeRequest().getRequestId();
    }

    @PostMapping("/status")
    public String workflowStatus(@RequestBody WorkflowRequest workflowRequest) {
        WorkflowDbEntity dbEntity = workflowDbService.findWorkflowId(workflowRequest.getDbId());
        IntakeWorkflow intakeWorkflow = workflowClient.newWorkflowStub(IntakeWorkflow.class , dbEntity.getWorkflowId());
        return intakeWorkflow.findCurrentStatus();
    }

    @PostMapping("/cancel-ops")
    public String cancelOpsTeamWorkflow(@RequestBody WorkflowRequest workflowRequest) {
        WorkflowDbEntity dbEntity = workflowDbService.findWorkflowId(workflowRequest.getDbId());
        OpsTeamWorkflow workflow = workflowClient.newWorkflowStub(OpsTeamWorkflow.class,"ops-" + dbEntity.getWorkflowId());
        workflow.cancelWork();  // Signal call
        return "🚨 Cancel signal sent to OpsTeamWorkflow";
    }

}
