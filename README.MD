## 📖 Temporal Spring Boot Vibe Coding Using ChatGpt 😂

| Day    | Click to View Section                                                                                                             |
|--------|-----------------------------------------------------------------------------------------------------------------------------------|
| 🔹 Day 1 | [Spring Boot Integration + Hello Workflow](#-day-1-integrate-temporal-java-sdk-with-spring-boot---helloworkflow-and-rest-trigger) |
| 🔹 Day 2 | [Retry Logic, Timers, Exception Handling](#-day-2-temporal-retry-logic-timers-and-exception-handling-spring-boot)                 |
| 🔹 Day 3 | [Signals, Flags, and Child Workflows](#-day-3-signals-flags-and-child-workflows)                                                  |                                                                                                             |
| 🔹 Day 4 | [Workflow Versioning & Patching in Temporal (Spring Boot)](#-day-4-workflow-versioning--patching-in-temporal-spring-boot) |
| 🔜 Day 5 | Queries, Interceptors, and Heartbeats                                                                                             |
| 🔜 Day 6 | Dynamic Workflow Composition (JSON API)                                                                                           |
| 🔜 Day 7 | Finalizing PaaS Layer for TempleRun                                                                                               |


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
        int random = (int)(Math.random() * 3) + 1;
        System.out.println("🧮 Radom:"+random);
        if (random == 2) {
            System.out.println("☹️ Simulated failure");
            throw new RuntimeException("Simulated failure in HelloActivity");
        }
        if (random == 3) {
            System.out.println("🙅‍♂️ Don't Retry this");
            throw new IllegalArgumentException("NonRetryableExceptions failure in HelloActivity");
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
        RetryOptions retryOptions = RetryOptions.newBuilder()
                .setInitialInterval(Duration.ofSeconds(1))
                .setBackoffCoefficient(2.0)
                .setMaximumAttempts(3)
                .setDoNotRetry(IllegalArgumentException.class.getName())
                .build();
        ActivityOptions options = ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofSeconds(3))
                .setRetryOptions(retryOptions)
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


# 📘 Day 3: Signals, Flags, and Child Workflows

This project demonstrates a **multi-phase Temporal workflow system** in Java using Spring Boot. It models a new internet service connection process, progressing through three major workflows:

- `IntakeWorkflow`: Starts the process, sets flags based on service availability, and waits for manager approval.
- `InstallationWorkflow`: Triggered upon approval; initiates OpsTeam activities.
- `OpsTeamWorkflow`: Executes concrete steps like reaching location, installing the service, and notifying the user.

---

## 🚀 How It Works

### Step 1: IntakeWorkflow

- Triggered via `/api/intake/start`.
- Calls three APIs to check:
    - ZIP availability
    - Bandwidth check
    - Address verification
- Sets internal flags: `SERVICE_AVAILABLE`, `ADDRESS_VALID`
- Waits for manager approval via signal
- Upon approval, starts `InstallationWorkflow` as a child workflow

### Step 2: InstallationWorkflow

- Triggered by `approveInstallation` signal from Intake
- Starts `OpsTeamWorkflow` as a child
- Passes entire `IntakeRequest` object forward

### Step 3: OpsTeamWorkflow

- Installs service in 3 steps:
    - `reachUserLocation`
    - `installedService`
    - `notifyUser` via email
- Each step is an activity

---

## 📦 Code Structure

## IntakeRequest.java

```java
@Getter
@Setter
public class IntakeRequest {
    private String requestId;
    private String zip;
    private String address;
    private String mbps;
    private String userEmailId;
}
```


## TaskQueue.java

```java
public class TaskQueue {
    public static class Name {
        public static final String INTAKE_TASK_QUEUE = "intake-task-queue";
        public static final String INSTALLATION_TASK_QUEUE = "installation-task-queue";
        public static final String OPS_TASK_QUEUE = "ops-task-queue";
    }
}
```

### 1. `IntakeWorkflow.java`

````java
@WorkflowInterface
public interface IntakeWorkflow {
    @WorkflowMethod
    void start(IntakeRequest intakeRequest);

    @SignalMethod
    void approveInstallation(IntakeRequest intakeRequest); // manager approval
}
````

### 2. `IntakeWorkflowImpl.java`
````java
public class IntakeWorkflowImpl implements IntakeWorkflow {
    private boolean approved = false;

    private final SiteCheckActivity siteCheck = Workflow.newActivityStub(
            SiteCheckActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .build()
    );

    @Override
    public void start(IntakeRequest intakeRequest) {
        System.out.println("📥 Intake received for: "+intakeRequest.getRequestId());
        boolean serviceAvailable = siteCheck.checkZipService(intakeRequest.getZip());
        boolean addressValid = siteCheck.validateAddress(intakeRequest.getAddress());
        boolean bandwidthOk = siteCheck.checkBandwidth(intakeRequest.getZip(), intakeRequest.getMbps());

        if (!serviceAvailable || !addressValid || !bandwidthOk) {
            System.out.println("❌ Pre-checks failed");
            return;
        }

        System.out.println("✅ All checks passed. 📋 Waiting for manager approval...");

        // Wait for approval signal
        Workflow.await(() -> approved);

        System.out.println("✅ Manager approved. Triggering InstallationWorkflow...");

        // Trigger child workflow
        InstallationWorkflow child = Workflow.newChildWorkflowStub(InstallationWorkflow.class);
        child.startInstallation(intakeRequest);
    }

    @Override
    public void approveInstallation(IntakeRequest intakeRequest) {
        System.out.println("📩 Received manager approval signal!:"+intakeRequest.getRequestId());
        this.approved = true;
    }
}
````

### 2a. `SiteCheckActivity.java`

````java
@ActivityInterface
public interface SiteCheckActivity {
    @ActivityMethod
    boolean checkZipService(String zipCode);
    @ActivityMethod
    boolean validateAddress(String address);
    @ActivityMethod
    boolean checkBandwidth(String zipCode, int requestedMbps);
}
````

### 2b. `SiteCheckActivityImpl.java`

````java
public class SiteCheckActivityImpl implements SiteCheckActivity {
    @Override
    public boolean checkZipService(String zipCode) {
        System.out.println("✅ Checking service availability for ZIP: " + zipCode);
        return zipCode.startsWith("7"); // Simulate
    }

    @Override
    public boolean validateAddress(String address) {
        System.out.println("📍 Validating address: " + address);
        return address != null && address.length() > 5;
    }

    @Override
    public boolean checkBandwidth(String zipCode, int requestedMbps) {
        System.out.println("🔌 Checking bandwidth availability for " + requestedMbps + " Mbps");
        return requestedMbps <= 500;
    }
}
````

---

### 3. `InstallationWorkflow.java`

````java
@WorkflowInterface
public interface InstallationWorkflow {
    @WorkflowMethod
    void startInstallation(IntakeRequest intakeRequest);
}
````

### 4. `InstallationWorkflowImpl.java`

````java
public class InstallationWorkflowImpl implements InstallationWorkflow {
    @Override
    public void startInstallation(IntakeRequest intakeRequest) {
        String zipCode = intakeRequest.getZip();
        String address = intakeRequest.getAddress();
        System.out.println("🚀 [InstallationWorkflow] Starting for ZIP: " + zipCode + ", Address: " + address);

        ChildWorkflowOptions childOptions = ChildWorkflowOptions.newBuilder()
                .setTaskQueue(TaskQueue.Name.OPS_TASK_QUEUE)
                .setWorkflowRunTimeout(Duration.ofMinutes(10))
                .build();

        OpsTeamWorkflow ops = Workflow.newChildWorkflowStub(OpsTeamWorkflow.class, childOptions);

        // Simulate steps
        ops.installService(intakeRequest);

        System.out.println("✅ [InstallationWorkflow] Installation steps completed.");
    }
}
````

---

### 5. `OpsTeamWorkflow.java`

````java
@WorkflowInterface
public interface OpsTeamWorkflow {
    @WorkflowMethod
    void installService(IntakeRequest intakeRequest);
}
````

### 6. `OpsTeamWorkflowImpl.java`

````java
public class OpsTeamWorkflowImpl implements OpsTeamWorkflow {
    @Override
    public void installService(IntakeRequest intakeRequest) {
        String zip = intakeRequest.getZip();
        String address = intakeRequest.getAddress();
        String email = intakeRequest.getUserEmailId();
        System.out.println("🔧 [OpsTeam] Installing service at ZIP: " + zip + ", Address: " + address);
        Workflow.sleep(Duration.ofSeconds(5));
        OpsTeamActivity opsTeamActivity = Workflow.newActivityStub(
                OpsTeamActivity.class,
                ActivityOptions.newBuilder()
                        .setStartToCloseTimeout(Duration.ofMinutes(1))
                        .setTaskQueue(TaskQueue.Name.OPS_TASK_QUEUE)
                        .build()
        );
        System.out.println("👨‍🔧 Starting OpsTeam Activities at user location...");

        opsTeamActivity.reachUserLocation(zip, address);
        opsTeamActivity.installedService(zip, address);
        opsTeamActivity.notifyUser(email);
    }
}
````

---

### 7. `OpsTeamActivity.java`

````java
@ActivityInterface
public interface OpsTeamActivity {
    @ActivityMethod
    void reachUserLocation(String zip, String address);
    @ActivityMethod
    void installedService(String zip, String address);
    @ActivityMethod
    void notifyUser(String email);
}
````

---

### 8. `OpsTeamActivityImpl.java`

````java
public class OpsTeamActivityImpl implements OpsTeamActivity {
    @Override
    public void reachUserLocation(String zip, String address) {
        System.out.println("🚗 [OpsTeam] Reaching user location at ZIP: " + zip + ", Address: " + address);
        // Simulate delay
        try {
            Thread.sleep(1000); // In real workflows, use Workflow.sleep() if inside a workflow
        } 
        catch (InterruptedException ignore) {}
    }

    @Override
    public void installedService(String zip, String address) {
        System.out.println("🚗 [OpsTeam] Installed Service at user location ZIP: " + zip + ", Address: " + address);
        // Simulate delay
        try {
            Thread.sleep(1000);
        } 
        catch (InterruptedException ignore) {}
    }

    @Override
    public void notifyUser(String email) {
        System.out.println("📧 [OpsTeam] Sending installation complete email to: " + email);
        try {
            Thread.sleep(500);
        } 
        catch (InterruptedException ignore) {}
    }
}
````

---

### 9. `IntakeController.java`

````java
@RestController
@RequestMapping("/api/intake")
public class IntakeController {
    private final WorkflowClient workflowClient;

    public IntakeController(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    @PostMapping("/start")
    public String startIntake(@RequestBody IntakeRequest intakeRequest) {
        IntakeWorkflow workflow = workflowClient.newWorkflowStub(
                IntakeWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setWorkflowId("intake-" + intakeRequest.getRequestId())
                        .setTaskQueue(TaskQueue.Name.INTAKE_TASK_QUEUE)
                        .build()
        );
        WorkflowClient.start(workflow::start, intakeRequest);
        return "Started IntakeWorkflow for: " + intakeRequest.getRequestId();
    }

    @PostMapping("/approve")
    public String approveIntake(@RequestBody IntakeRequest intakeRequest) {
        IntakeWorkflow intakeWorkflow = workflowClient.newWorkflowStub(IntakeWorkflow.class, "intake-" + intakeRequest.getRequestId());
        intakeWorkflow.approveInstallation(intakeRequest);
        return "Manager approved intake for: " + intakeRequest.getRequestId();
    }
}
````

---

## 🚀 How to Test

1. Start the Temporal server and Spring Boot app
2. `POST /api/intake/start` with body:

````json
{
  "requestId": "req-1001",
  "zip": "75001",
  "address": "123 Main Street",
  "mbps": "100",
  "userEmailId": "ishu@example.com"
}
````

3. Simulate approval:  `POST /api/intake/approve`
````json
{
  "requestId": "req-1001",
  "zip": "75001",
  "address": "123 Main Street",
  "mbps": "100",
  "userEmailId": "ishu@example.com"
}
````

4. ✅ Final Output (Logs):
```
📥 Intake received for: req-1001
✅ Checking service availability for ZIP: 75001
📍 Validating address: 123 Main Street
🔌 Checking bandwidth availability for 100 Mbps
✅ All checks passed. 📋 Waiting for manager approval...
📩 Received manager approval signal!:req-1001
✅ Manager approved. Triggering InstallationWorkflow...
🚀 [InstallationWorkflow] Starting for ZIP: 75001, Address: 123 Main Street
🔧 [OpsTeam] Installing service at ZIP: 75001, Address: 123 Main Street
👨‍🔧 Starting OpsTeam Activities at user location...
🚗 [OpsTeam] Reaching user location at ZIP: 75001, Address: 123 Main Street
🚗 [OpsTeam] Installed Service at user location ZIP: 75001, Address: 123 Main Street
📧 [OpsTeam] Sending installation complete email to: ishu@example.com
✅ [InstallationWorkflow] Installation steps completed.
```

### 🧠 Key Concepts Recap

#### ✅ 1. Signals

| Concept         | Description                                                                 |
|----------------|-----------------------------------------------------------------------------|
| What are they? | Signals are Temporal’s way to send data to a **running workflow**.          |
| Why use them?  | They let workflows **pause and wait for external input**, like REST calls.  |
| Where used?    | To notify `IntakeWorkflow` when **manager approval** was received.          |
| Example        | `@SignalMethod void approveInstallation(IntakeRequest intakeRequest);`      |

---

#### ✅ 2. Flags (Decision Determinants)

| Concept         | Description                                                                 |
|----------------|------------------------------------------------------------------------------|
| What are they? | In-memory boolean variables inside the workflow to **guide logic flow**.     |
| Why use them?  | To track **if pre-checks passed**, and whether to proceed.                   |
| Where used?    | Inside `IntakeWorkflow` for `SERVICE_AVAILABLE`, `ADDRESS_VALID`, etc.       |
| Example        | `if (serviceAvailable && addressValid) { continueWorkflow(); }`              |

---

#### ✅ 3. Child Workflows

| Concept         | Description                                                                 |
|----------------|-----------------------------------------------------------------------------|
| What are they? | A workflow calling **another workflow**, fully managed by Temporal.         |
| Why use them?  | Modularity, independent retry/failure handling, traceability.               |
| Where used?    | From `InstallationWorkflow` → called `OpsTeamWorkflow` as child             |
| Example        | `OpsTeamWorkflow ops = Workflow.newChildWorkflowStub(...);`                 |

---

#### ✅ 4. Activities and Timeouts

| Concept         | Description                                                                 |
|----------------|-----------------------------------------------------------------------------|
| What are they? | Real-world steps executed as **Activities** in Temporal                     |
| Examples        | `reachUserLocation()`, `notifyUser()`, etc.                                |
| Timeout Used    | `StartToCloseTimeout` for each activity to prevent stuck tasks             |
| Why Important?  | Helps Temporal manage retries and detect long-running/stuck operations     |

---

### 🧠 Summary

- ✅ Used **Signals** to pause/resume workflow on external approval
- ✅ Introduced **Flags** to track decision paths inside workflow logic
- ✅ Triggered **Child Workflows** (OpsTeam) from Installation phase
- ✅ Encapsulated real-world steps inside **Activities** with timeouts
- ✅ Improved traceability and modularity of the workflow system

---

# 📘 Day 4: Workflow Versioning & Patching in Temporal (Spring Boot)

In this lesson, we learn how to **safely evolve Temporal workflows** over time without breaking running or replaying workflows. This is crucial in long-running workflows, where **modifying activity structure without versioning will lead to runtime errors** like `NonDeterministicException`.

---

## 🧠 Key Concepts

- `Workflow.getVersion(changeId, minVersion, maxVersion)` is used to **gate** new logic during workflow execution.
- Without versioning, **adding, removing or reordering activities** causes workflow replay to fail.
- Temporal needs to **reproduce the exact sequence** of commands for a workflow run, so even code comments or refactors (if not guarded) can break determinism.
- `Workflow.isReplaying()` is deprecated; always prefer `getVersion`.

---

## 🔧 Scenario: Adding New Activities Mid-Workflow

We enhanced the `IntakeWorkflow` and `InstallationWorkflow` by adding new logic:
- `validateAddress1()` — an extra address check.
- `verifyCableConnection()` — a cable validation step during Ops installation.

---

## 💥 Problem Without Versioning

If you directly add the new activity like:

```java
// ❌ BREAKS Replay
boolean addressValid1 = siteCheck.validateAddress1(intakeRequest.getAddress());
```

You’ll get:

```
NonDeterministicException: Command COMMAND_TYPE_SCHEDULE_ACTIVITY_TASK doesn't match event EVENT_TYPE_ACTIVITY_TASK_SCHEDULED
```

Why? Because the event history from previous runs **doesn’t expect** a new activity to be scheduled at that point.

---

## ✅ Safe Fix With Versioning

### In `IntakeWorkflowImpl.java`:

```java
int version = Workflow.getVersion("validateAddress1", Workflow.DEFAULT_VERSION, 1);
boolean addressValid1 = true;
if (version == 1) {
    addressValid1 = siteCheck.validateAddress1(intakeRequest.getAddress());
}
```

### In `OpsTeamWorkflowImpl.java`:

```java
int version = Workflow.getVersion("verifyCableConnection", Workflow.DEFAULT_VERSION, 1);
if (version == 1) {
    opsTeamActivity.verifyCableConnection(zip, address);
}
```

---

## 🔁 How Versioning Works

- The first time workflow runs, `getVersion(...)` writes the version into workflow history.
- On replay, Temporal reads this history value and feeds it back.
- Thus the same path (either legacy or new) is followed, ensuring determinism.

---

## 🧪 How to Test Breakage (and Fix)

### Test Breakage

1. Start a workflow by hitting the `/start` API.
2. Before calling `/approve`, **add a new activity** in your workflow **without versioning**.
3. Now call `/approve` — Temporal will **crash on replay** with `NonDeterministicException`.

### Fix

- Wrap new logic inside `Workflow.getVersion(...)`.
- Redeploy and re-run — now old and new workflows coexist safely.

---

## ✅ Summary

| Feature                 | Covered |
|-------------------------|---------|
| Add activity mid-flow   | ✅      |
| Prevent breaking changes| ✅      |
| Use of `getVersion()`   | ✅      |
| End-to-end test         | ✅      |

---