package com.salilvnair.temporal.interceptor.config;

import com.salilvnair.temporal.interceptor.provider.LoggingActivityInterceptor;
import com.salilvnair.temporal.interceptor.provider.LoggingWorkflowInboundInterceptor;
import io.nexusrpc.handler.OperationContext;
import io.temporal.common.interceptors.ActivityInboundCallsInterceptor;
import io.temporal.common.interceptors.NexusOperationInboundCallsInterceptor;
import io.temporal.common.interceptors.WorkerInterceptor;
import io.temporal.common.interceptors.WorkflowInboundCallsInterceptor;

public class LoggingWorkerInterceptor implements WorkerInterceptor {

    @Override
    public WorkflowInboundCallsInterceptor interceptWorkflow(WorkflowInboundCallsInterceptor next) {
        return new LoggingWorkflowInboundInterceptor(next);
    }

    @Override
    public ActivityInboundCallsInterceptor interceptActivity(ActivityInboundCallsInterceptor next) {
        return new LoggingActivityInterceptor(next);
    }

    @Override
    public NexusOperationInboundCallsInterceptor interceptNexusOperation(OperationContext context, NexusOperationInboundCallsInterceptor next) {
        return next;
    }
}