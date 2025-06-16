## 📖 Temporal Spring Boot Vibe Coding Using ChatGpt 😂

| Day    | Click to View Section                                                                                                            |
|--------|----------------------------------------------------------------------------------------------------------------------------------|
| 🔹 Day 1 | [Spring Boot Integration + Hello Workflow](#-day-1-integrate-temporal-java-sdk-with-spring-boot---helloworkflow-and-rest-trigger) |
| 🔹 Day 2 | [Retry Logic, Timers, Exception Handling](#-day-2-temporal-retry-logic-timers-and-exception-handling-spring-boot)                |
| 🔜 Day 3 | Signals and Queries                                                                                                              |
| 🔜 Day 4 | Child Workflows                                                                                                                  |
| 🔜 Day 5 | Workflow Versioning & Patching                                                                                                   |
| 🔜 Day 6 | Dynamic Workflow Composition (JSON API)                                                                                          |
| 🔜 Day 7 | Finalizing PaaS Layer for TempleRun                                                                                              |


# 📘 Day 1: Integrate Temporal Java SDK with Spring Boot - HelloWorkflow and REST Trigger

### ✅ Objective
Create a basic Temporal setup with:
- Spring Boot integration
- Simple `HelloWorkflow` + `HelloActivity`
- REST API `/api/greet` triggers workflow execution
- No Docker — uses local Temporal server
- Visible in [Temporal Web UI](http://localhost:8233)

---

### 📦 Project Structure

```
temporal-springboot-demo/
├── controller/
│   └── HelloController.java
├── workflow/
│   ├── HelloWorkflow.java
│   └── HelloWorkflowImpl.java
├── activity/
│   ├── HelloActivity.java
│   └── HelloActivityImpl.java
├── config/
│   └── TemporalConfig.java
└── TemporalSpringBootDemoApplication.java
```

---

### 🔧 Maven Dependencies (`pom.xml`)

```xml
<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  <dependency>
    <groupId>io.temporal</groupId>
    <artifactId>temporal-sdk</artifactId>
    <version>1.24.1</version>
  </dependency>
</dependencies>
```

---

### 🚀 Main App Entry

```java
@SpringBootApplication
public class TemporalSpringBootDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(TemporalSpringBootDemoApplication.class, args);
    }
}
```

---

### ⚙️ Temporal Configuration (`TemporalConfig.java`)

```java
@Configuration
public class TemporalConfig {
    
    public static final String TASK_QUEUE = "hello-task-queue";
    
    @Bean
    public WorkflowServiceStubs serviceStubs() {
        return WorkflowServiceStubs.newInstance();
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs service) {
        return WorkflowClient.newInstance(service);
    }

    @Bean
    public WorkerFactory workerFactory(WorkflowClient client) {
        return WorkerFactory.newInstance(client);
    }

    @Bean
    public Worker worker(WorkerFactory factory) {
        Worker worker = factory.newWorker(TASK_QUEUE);
        worker.registerWorkflowImplementationTypes(HelloWorkflowImpl.class);
        worker.registerActivitiesImplementations(new HelloActivityImpl());
        factory.start();
        return worker;
    }
}
```

---

### 👷 Hello Activity

#### Interface

```java
public interface HelloActivity {
    String sayHello(String name);
}
```

#### Implementation

```java
public class HelloActivityImpl implements HelloActivity {
    @Override
    public String sayHello(String name) {
        return "👋 Hello from activity, " + name;
    }
}
```

---

### 🔁 Hello Workflow

#### Interface

```java
@WorkflowInterface
public interface HelloWorkflow {
    @WorkflowMethod
    void run(String name);
}
```

#### Implementation

```java
public class HelloWorkflowImpl implements HelloWorkflow {
    private final HelloActivity activity;

    public HelloWorkflowImpl() {
        ActivityOptions options = ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofSeconds(5))
                .build();
        this.activity = Workflow.newActivityStub(HelloActivity.class, options);
    }

    @Override
    public void run(String name) {
        String result = activity.sayHello(name);
        System.out.println("✅ Workflow Result: " + result);
    }
}
```

---

### 🌐 REST Controller – `HelloController`

```java
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
```

---

### ✅ Test

Visit:
```
http://localhost:8080/api/greet?name=Ishu
```

🧭 Then go to [http://localhost:8233](http://localhost:8233) to see your workflow run in Temporal UI!

---

### 🧠 Key Takeaways

| Concept          | What it Does |
|------------------|--------------|
| `HelloWorkflow`  | Defines a workflow method triggered by REST |
| `HelloActivity`  | A real-world task executed by the worker |
| `WorkflowClient` | Starts the workflow via Java API |
| `Worker`         | Listens to the task queue and runs workflow/activity |
| Temporal UI      | Observability of workflow execution |


# 📘 Day 2: Temporal Retry Logic, Timers, and Exception Handling (Spring Boot)

---

### ✅ Objectives
- Configure retry policies on activities
- Add delay/timer support using `Workflow.sleep`
- Handle activity failures gracefully
- Observe retry behavior in Temporal Web UI

---

### ⚙️ Changes Made

---

### 🔁 Updated `HelloActivityImpl` (random failure to test retries)

```java
public class HelloActivityImpl implements HelloActivity {
    @Override
    public String sayHello(String name) {
        System.out.println("👋 Trying to greet: " + name);

        if (Math.random() < 0.7) {
            System.out.println("💥 Simulated failure");
            throw new RuntimeException("Simulated failure in HelloActivity");
        }

        return "Hello from activity, " + name;
    }
}
```

---

### 🔄 Updated `HelloWorkflowImpl` to add:

- Retry options
- Timer (Workflow.sleep)
- Exception handling

```java
public class HelloWorkflowImpl implements HelloWorkflow {
    private final HelloActivity activity;

    public HelloWorkflowImpl() {
        ActivityOptions options = ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofSeconds(3))
                .setRetryOptions(RetryOptions.newBuilder()
                        .setInitialInterval(Duration.ofSeconds(1))
                        .setBackoffCoefficient(2.0)
                        .setMaximumAttempts(3)
                        .build())
                .build();

        this.activity = Workflow.newActivityStub(HelloActivity.class, options);
    }

    @Override
    public void run(String name) {
        try {
            // Simulate delay
            Workflow.sleep(Duration.ofSeconds(2));

            String result = activity.sayHello(name);
            System.out.println("✅ Workflow Result: " + result);
        } catch (Exception ex) {
            System.out.println("❌ Activity failed after retries: " + ex.getMessage());
            throw Workflow.wrap(ex); // required to propagate error correctly in Temporal
        }
    }
}
```

---

### 🌐 REST API – No Changes Needed

You can still test using:

```
http://localhost:8080/api/greet?name=Ishu
```

---

### 📊 Verify in Temporal UI

Visit: [http://localhost:8233](http://localhost:8233)

- Click into a workflow run
- Look at `Attempts` under Activity Execution
- See retry delays, backoff, and final failure (if max attempts exceeded)

---

### 🧠 Key Concepts Recap

| Feature          | Description |
|------------------|-------------|
| RetryOptions     | Auto-retries with backoff for failed activities |
| Workflow.sleep   | Non-blocking delay inside workflows |
| Workflow.wrap()  | Needed to rethrow Java exceptions from within workflow code |
| Max Attempts     | Stops retrying after limit; marks workflow as failed |

---

### ✅ Summary

- Implemented **retry policy** on activity calls
- Used `Workflow.sleep()` to simulate delays/timers
- Observed retries and failures in the Temporal UI

---