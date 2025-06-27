package com.salilvnair.temporal.config;

import com.salilvnair.temporal.interceptor.config.LoggingClientInterceptor;
import com.salilvnair.temporal.interceptor.config.LoggingWorkerInterceptor;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerFactoryOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemporalConfig {

    @Value("${temporal.log.client.interceptor:false}")
    private boolean logClientInterceptor;

    @Value("${temporal.log.worker.interceptor:false}")
    private boolean logWorkerInterceptor;


    @Bean
    public WorkflowServiceStubs workflowServiceStubs() {
        return WorkflowServiceStubs.newServiceStubs(
                WorkflowServiceStubsOptions.newBuilder()
                        .setTarget("localhost:7233")  // non-docker Temporal instance
                        .build()
        );
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs service) {
        WorkflowClientOptions.Builder builder = WorkflowClientOptions.newBuilder().setNamespace("default");
        if(logClientInterceptor) {
            builder.setInterceptors(new LoggingClientInterceptor());
        }
        return WorkflowClient.newInstance(service, builder.build());
    }

    @Bean
    public WorkerFactory workerFactory(WorkflowClient client) {
        WorkerFactoryOptions.Builder builder = WorkerFactoryOptions.newBuilder();
        if(logWorkerInterceptor) {
            builder.setWorkerInterceptors(new LoggingWorkerInterceptor());
        }
        return WorkerFactory.newInstance(client, builder.build());
    }
}