package com.salilvnair.temporal.workflow;

import com.salilvnair.temporal.model.IntakeRequest;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface SupportSetupWorkflow {
    @WorkflowMethod
    void startSupportSetup(IntakeRequest intakeRequest);
}
