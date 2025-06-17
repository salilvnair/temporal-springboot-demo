package com.salilvnair.temporal.interceptor.provider;

import io.temporal.common.interceptors.WorkflowClientCallsInterceptor;
import io.temporal.common.interceptors.WorkflowClientCallsInterceptorBase;

public class LoggingWorkflowClientInterceptor extends WorkflowClientCallsInterceptorBase {
    public LoggingWorkflowClientInterceptor(WorkflowClientCallsInterceptor next) {
        super(next);
    }

    @Override
    public WorkflowStartOutput start(WorkflowStartInput input) {
        System.out.println("🚀 [LoggingWorkflowClientInterceptor] Starting workflow: " + input.getWorkflowId());
        return super.start(input);
    }

    @Override
    public WorkflowSignalOutput signal(WorkflowSignalInput input) {
        System.out.println("📩 [LoggingWorkflowClientInterceptor] Sending signal to: " + input.getWorkflowExecution().getWorkflowId());
        return super.signal(input);
    }
}
