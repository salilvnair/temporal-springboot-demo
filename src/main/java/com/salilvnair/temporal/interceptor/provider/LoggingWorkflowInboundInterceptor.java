package com.salilvnair.temporal.interceptor.provider;

import io.temporal.common.interceptors.WorkflowInboundCallsInterceptor;
import io.temporal.common.interceptors.WorkflowInboundCallsInterceptorBase;
import io.temporal.common.interceptors.WorkflowOutboundCallsInterceptor;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInfo;

public class LoggingWorkflowInboundInterceptor extends WorkflowInboundCallsInterceptorBase {

    private WorkflowInfo workflowInfo;

    public LoggingWorkflowInboundInterceptor(WorkflowInboundCallsInterceptor next) {
        super(next);
    }

    @Override
    public void init(WorkflowOutboundCallsInterceptor outboundCalls) {
        this.workflowInfo = Workflow.getInfo(); // Capture info about the current workflow
        System.out.println("📥 [LoggingWorkflowInboundInterceptor:init] Initialized for Workflow ID: " + workflowInfo.getWorkflowId());
        super.init(outboundCalls);
    }

    @Override
    public WorkflowOutput execute(WorkflowInput input) {
        System.out.println("🚀 [LoggingWorkflowInboundInterceptor:execute] Starting workflow: " + workflowInfo.getWorkflowType());
        WorkflowOutput output = super.execute(input);
        System.out.println("✅ [LoggingWorkflowInboundInterceptor:execute] Completed workflow: " + workflowInfo.getWorkflowType());
        return output;
    }

    @Override
    public void handleSignal(SignalInput input) {
        System.out.println("📨 [LoggingWorkflowInboundInterceptor:signal] Received signal: " + input.getSignalName());
        super.handleSignal(input);
    }

    @Override
    public QueryOutput handleQuery(QueryInput input) {
        System.out.println("🔍 [LoggingWorkflowInboundInterceptor:query] Received query: " + input.getQueryName());
        return super.handleQuery(input);
    }
}
