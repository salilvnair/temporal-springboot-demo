package com.salilvnair.temporal.interceptor.provider;

import io.temporal.activity.ActivityExecutionContext;
import io.temporal.common.interceptors.ActivityInboundCallsInterceptor;
import io.temporal.common.interceptors.ActivityInboundCallsInterceptorBase;

public class LoggingActivityInterceptor extends ActivityInboundCallsInterceptorBase {
    private ActivityExecutionContext context;

    public LoggingActivityInterceptor(ActivityInboundCallsInterceptor next) {
        super(next);
    }

    @Override
    public void init(ActivityExecutionContext context) {
        super.init(context);
        this.context = context;
        System.out.println("🔧 [LoggingActivityInterceptor:init] Initialized for activity: " + context.getInfo().getActivityType());
    }

    @Override
    public ActivityOutput execute(ActivityInput input) {
        String activityType = context.getInfo().getActivityType();
        System.out.println("🚀 [LoggingActivityInterceptor:execute] Starting activity: " + activityType);

        ActivityOutput output = super.execute(input);

        System.out.println("✅ [LoggingActivityInterceptor:execute] Finished activity: " + activityType);
        return output;
    }
}
