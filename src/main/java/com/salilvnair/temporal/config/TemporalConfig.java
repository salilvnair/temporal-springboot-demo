package com.salilvnair.temporal.config;

import com.salilvnair.temporal.interceptor.config.LoggingClientInterceptor;
import com.salilvnair.temporal.interceptor.config.LoggingWorkerInterceptor;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerFactoryOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemporalConfig {


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
        return WorkflowClient.newInstance(
                service,
                WorkflowClientOptions.newBuilder()
                        .setNamespace("default")
                        .setInterceptors(new LoggingClientInterceptor())
                        .build()
        );
    }

    @Bean
    public WorkerFactory workerFactory(WorkflowClient client) {
        WorkerFactoryOptions factoryOptions = WorkerFactoryOptions.newBuilder()
                .setWorkerInterceptors(new LoggingWorkerInterceptor())
                .build();

        return WorkerFactory.newInstance(client, factoryOptions);
    }
}