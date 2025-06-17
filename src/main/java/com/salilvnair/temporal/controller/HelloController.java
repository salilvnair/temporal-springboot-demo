package com.salilvnair.temporal.controller;

import com.salilvnair.temporal.type.TaskQueue;
import com.salilvnair.temporal.workflow.HelloWorkflow;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
                        .setTaskQueue(TaskQueue.Name.HELLO_TASK_QUEUE)
                        .setWorkflowId("greet-workflow-1")
                        .build()
        );

        WorkflowExecution execution = WorkflowClient.start(workflow::run, name);
        return "Started workflow with ID: " + execution.getWorkflowId();
    }

    @GetMapping("/greet/update")
    public String updateGreeting(@RequestParam String message) {
        WorkflowStub stub = workflowClient.newUntypedWorkflowStub("greet-workflow-1");
        stub.signal("updateGreeting", message);
        return "Signal sent to update greeting";
    }

    @GetMapping("/greet/query")
    public String queryGreeting() {
        WorkflowStub stub = workflowClient.newUntypedWorkflowStub("greet-workflow-1");
        // Send query directly
        String result = stub.query("getGreeting", String.class);

        return "🔍 Greeting is: " + result;
    }
}