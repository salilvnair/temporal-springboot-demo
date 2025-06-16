package com.salilvnair.temporal.controller;

import com.salilvnair.temporal.config.TemporalConfig;
import com.salilvnair.temporal.workflow.HelloWorkflow;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class HelloController {

    private final WorkflowClient workflowClient;

    public HelloController(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    @GetMapping("/greet")
    public String greet(@RequestParam String name) {
        HelloWorkflow workflow = workflowClient.newWorkflowStub(
                HelloWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TemporalConfig.TASK_QUEUE)
                        .build()
        );

        WorkflowExecution execution = WorkflowClient.start(workflow::run, name);
        return "Started workflow with ID: " + execution.getWorkflowId();
    }
}