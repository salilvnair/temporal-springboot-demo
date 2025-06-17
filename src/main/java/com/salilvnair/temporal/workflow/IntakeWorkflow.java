package com.salilvnair.temporal.workflow;

import com.salilvnair.temporal.model.IntakeRequest;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface IntakeWorkflow {
    @WorkflowMethod
    void start(IntakeRequest intakeRequest);

    @SignalMethod
    void approveInstallation(IntakeRequest intakeRequest); // manager approval

    // 🔍 New query method
    @QueryMethod
    String findCurrentStatus();
}