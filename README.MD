## 📖 Temporal Spring Boot Vibe Coding Using ChatGpt 😂

| Day       | Click to View Section                                                                                                                       |
|-----------|----------------------------------------------------------------------------------------------------------------------------------------------|
| ✅ Day 1   | [Spring Boot Integration + Hello Workflow](#-day-1-integrate-temporal-java-sdk-with-spring-boot---helloworkflow-and-rest-trigger)           |
| ✅ Day 2   | [Retry Logic, Timers, Exception Handling](#-day-2-temporal-retry-logic-timers-and-exception-handling-spring-boot)                            |
| ✅ Day 3   | [Signals, Flags, and Child Workflows](#-day-3-signals-flags-and-child-workflows)                                                             |
| ✅ Day 4   | [Workflow Versioning & Patching in Temporal (Spring Boot)](#-day-4-workflow-versioning--patching-in-temporal-spring-boot)                    |
| ✅ Day 5   | [Temporal Queries, Interceptors & Heartbeats (Spring Boot)](#-day-5-temporal-queries-interceptors--heartbeats-spring-boot)                   |
| ✅ Day 6   | [Multi-Phase Telecom Workflow – Provisioning, Billing, and Support Setup](#-day-6-multi-phase-telecom-workflow--provisioning-billing-and-support-setup) |
| 🔜 Day 7   | [Continue-As-New, SignalWithStart & Long-Running Workflows](#-day-7-continue-as-new-signalwithstart--long-running-workflows)                 |
| 🔜 Day 8   | [Workflow Updates, Search Attributes & Visibility APIs](#-day-8-workflow-updates-search-attributes--visibility-apis)                         |
| 🔜 Day 9   | [Retry Policies & Workflow-level Retry Config](#-day-9-retry-policies--workflow-level-retry-config)                                          |
| 🔜 Day 10  | [Testing, Sticky Queues, and Build ID Versioning](#-day-10-testing-sticky-queues-and-build-id-versioning)                                     |
| 🔜 Day 11  | [Pause/Resume Workflows via Signals + Interceptors](#-day-11-pauseresume-workflows-via-signals--interceptors)                                |
| 🔜 Day 12  | [Workflow Shadowing & Replay Testing](#-day-12-workflow-shadowing--replay-testing)                                                           |
| 🔜 Day 13  | [Dynamic Workflow Dispatch with JSON-Driven APIs](#-day-13-dynamic-workflow-dispatch-with-json-driven-apis)                                  |
| 🔜 Day 14  | [Workflow Deployment Best Practices in Production](#-day-14-workflow-deployment-best-practices-in-production)                                |



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

# 🫀 Day 5: Temporal Queries, Interceptors & Heartbeats (Spring Boot)

Welcome to Day 5 of our Temporal deep dive! Today, we tackled **Queries**, **Interceptors**, and **Heartbeats** to make our workflows more observable and controllable.

---

## ✅ 1. Workflow Queries (Read-Only Introspection)

We implemented `@QueryMethod` in `IntakeWorkflow`:

```java
@WorkflowInterface
public interface IntakeWorkflow {
  @WorkflowMethod
  void start(IntakeRequest intakeRequest);

  @SignalMethod
  void approveInstallation(IntakeRequest intakeRequest); // manager approval

  // 🔍 New query method
  @QueryMethod
  String findCurrentStatus();
}
```

```java
public class IntakeWorkflowImpl implements IntakeWorkflow {
  private boolean approved = false;
  private String status = "Not started";

  private final SiteCheckActivity siteCheck = Workflow.newActivityStub(
          SiteCheckActivity.class,
          ActivityOptions.newBuilder()
                  .setStartToCloseTimeout(Duration.ofSeconds(10))
                  .build()
  );

  @Override
  public void start(IntakeRequest intakeRequest) {
    status = "🏃‍♂️Running pre-checks...";
    System.out.println("📥 Intake received for: "+intakeRequest.getRequestId());
    boolean serviceAvailable = siteCheck.checkZipService(intakeRequest.getZip());
    boolean addressValid = siteCheck.validateAddress(intakeRequest.getAddress());
//        boolean addressValid1 = siteCheck.validateAddress1(intakeRequest.getAddress());
//        ❌ io.temporal.worker.NonDeterministicException:
//        [TMPRL1100] COMMAND_TYPE_SCHEDULE_ACTIVITY_TASK doesn't match EVENT_TYPE_ACTIVITY_TASK_SCHEDULED...
//        🚨 Why You're Getting This Error
//
//        Temporal records every step of the workflow into its event history — including activity method names, parameters, and order.
//
//        You changed the order or added a new activity:
//
////    previously:
//        validateAddress();      // Event ID 16
//        checkBandwidth();       // Event ID 17
//
////    now (you added in between):
//        validateAddress();      // Event ID 16
//        validateAddress1();     // NEW — not recorded before
//        checkBandwidth();       // Now this becomes Event ID 18
    boolean addressValid1 = true;
    int version = Workflow.getVersion("validateAddress1", Workflow.DEFAULT_VERSION, 1);
    if (version == 1) {
      addressValid1 = siteCheck.validateAddress1(intakeRequest.getAddress());
      if (!addressValid1) {
        status = "Pre-checks failed ❌";
        System.out.println("❌ Address validation 1 failed");
        return;
      }
    }

    boolean bandwidthOk = siteCheck.checkBandwidth(intakeRequest.getZip(), intakeRequest.getMbps());

    if (!serviceAvailable || !addressValid || !bandwidthOk) {
      status = "Pre-checks failed ❌";
      System.out.println("❌ Pre-checks failed");
      return;
    }

    System.out.println("✅ All checks passed. 📋 Waiting for manager approval...");

    // Wait for approval signal
    status = "Waiting for manager approval...👮‍♂️⏱️";
    Workflow.await(() -> approved);
    status = "👮‍♂️✅ Manager approved. Triggering InstallationWorkflow...";
    System.out.println("✅ Manager approved. Triggering InstallationWorkflow...");

    // Trigger child workflow
    InstallationWorkflow child = Workflow.newChildWorkflowStub(InstallationWorkflow.class);
    child.startInstallation(intakeRequest);
    status = "Installation complete 🚀";
  }

  @Override
  public void approveInstallation(IntakeRequest intakeRequest) {
    System.out.println("📩 Received manager approval email:"+intakeRequest.getRequestId());
    status = "📩 Received manager approval email";
    this.approved = true;
  }

  @Override
  public String findCurrentStatus() {
    return status;
  }
}

```

📌 This lets us query the live status of a running workflow via REST or internal logic.

---

## ✅ 2. Interceptors

Interceptors in Temporal allow injecting custom logic into SDK lifecycle points.

We implemented and registered:

### ▶️ `LoggingActivityInterceptor`

```java
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
```

### ▶️ `LoggingWorkflowInboundInterceptor`

```java
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
```

### ▶️ Registration via WorkerInterceptor

```java
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
```

### ✅ Registered via `WorkerFactoryOptions`:

```java
@Bean
public WorkerFactory workerFactory(WorkflowClient client) {
  WorkerFactoryOptions factoryOptions = WorkerFactoryOptions.newBuilder()
          .setWorkerInterceptors(new LoggingWorkerInterceptor())
          .build();

  return WorkerFactory.newInstance(client, factoryOptions);
}
```

### ▶️ Registration via WorkflowClientInterceptor

```java
public class LoggingClientInterceptor implements WorkflowClientInterceptor {
    @Override
    public WorkflowStub newUntypedWorkflowStub(String workflowType, WorkflowOptions options, WorkflowStub next) {
        return next;
    }

    @Override
    public WorkflowStub newUntypedWorkflowStub(WorkflowExecution execution, Optional<String> workflowType, WorkflowStub next) {
        return next;
    }

    @Override
    public ActivityCompletionClient newActivityCompletionClient(ActivityCompletionClient next) {
        return next;
    }

    @Override
    public WorkflowClientCallsInterceptor workflowClientCallsInterceptor(WorkflowClientCallsInterceptor next) {
        return new LoggingWorkflowClientInterceptor(next);
    }
}
```

### ✅ Registered via `WorkflowClientOptions`:

```java
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
```
---

### ✅ 3. Heartbeats + Activity Cancellation

#### ▶️ Heartbeat Activities

```java
@Override
public void installedService(String zip, String address) {
  ActivityExecutionContext context = Activity.getExecutionContext();
  System.out.println("🚗 [OpsTeam] Installed Service at user location ZIP: " + zip + ", Address: " + address);
  // Simulate delay
  try {
    for (int i = 1; i <= 5; i++) {
      context.heartbeat("Progress: " + (i * 20) + "%");
      Thread.sleep(1000);
    }
    Thread.sleep(1000); // In real workflows, use Workflow.sleep() if inside a workflow
  }
  catch (CanceledFailure e) {
    System.out.println("❌ Activity cancelled: " + e.getMessage());
    throw e;
  }
  catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    throw new RuntimeException(e);
  }
}
```

This helps detect long-running activity health.

#### ▶️ Signal-Based Workflow Cancellation

```java
@WorkflowInterface
public interface OpsTeamWorkflow {
  @WorkflowMethod
  void installService(IntakeRequest intakeRequest);

  @SignalMethod
  void cancelWork();
}
```

```java
public class OpsTeamWorkflowImpl implements OpsTeamWorkflow {
  private boolean cancelled = false;

  @Override
  public void installService(IntakeRequest intakeRequest) {
    System.out.println("🔧 [OpsTeam] Starting installation");
    if (cancelled) {
      System.out.println("❌ [OpsTeam] Cancelled before starting");
      return;
    }
    String zip = intakeRequest.getZip();
    String address = intakeRequest.getAddress();
    String email = intakeRequest.getUserEmailId();
    System.out.println("🔧 [OpsTeam] Installing service at ZIP: " + zip + ", Address: " + address);
    OpsTeamActivity opsTeamActivity = Workflow.newActivityStub(
            OpsTeamActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofMinutes(1))
                    .setHeartbeatTimeout(Duration.ofSeconds(5)) // heartbeat every 5 sec expected
                    .setTaskQueue(TaskQueue.Name.OPS_TASK_QUEUE)
                    .build()
    );
    System.out.println("👨‍🔧 Starting OpsTeam Activities at user location...");
    opsTeamActivity.reachUserLocation(zip, address);
    Workflow.sleep(Duration.ofSeconds(10));
    if (cancelled) {
      System.out.println("❌ [OpsTeam] Cancelled after reaching location");
      return;
    }
//        opsTeamActivity.verifyCableConnection(zip, address);
    int version = Workflow.getVersion("verifyCableConnection", Workflow.DEFAULT_VERSION, 1);
    if (version == 1) {
      opsTeamActivity.verifyCableConnection(zip, address);
    }

    opsTeamActivity.installedService(zip, address);
    if (cancelled) {
      System.out.println("❌ [OpsTeam] Cancelled after service install");
      return;
    }
    opsTeamActivity.notifyUser(email);
  }

  @Override
  public void cancelWork() {
    cancelled = true;
    System.out.println("🚨 [OpsTeam] Received cancel signal");
  }
}
```
In the `InstallationWorkflowImpl` added `workflowId`
```java
public class InstallationWorkflowImpl implements InstallationWorkflow {
    @Override
    public void startInstallation(IntakeRequest intakeRequest) {
        String zipCode = intakeRequest.getZip();
        String address = intakeRequest.getAddress();
        System.out.println("🚀 [InstallationWorkflow] Starting for ZIP: " + zipCode + ", Address: " + address);

        ChildWorkflowOptions childOptions = ChildWorkflowOptions.newBuilder()
                                            .setTaskQueue(TaskQueue.Name.OPS_TASK_QUEUE)
                                            .setWorkflowId("ops-" + intakeRequest.getRequestId())
                                            .setWorkflowRunTimeout(Duration.ofMinutes(10))
                                            .build();

        OpsTeamWorkflow ops = Workflow.newChildWorkflowStub(OpsTeamWorkflow.class, childOptions);

        // Simulate steps
        ops.installService(intakeRequest);

        System.out.println("✅ [InstallationWorkflow] Installation steps completed.");
    }
}
```


And `cancel-ops` in `IntakeController.java`

```java
@PostMapping("/cancel-ops")
public String cancelOpsTeamWorkflow(@RequestBody IntakeRequest intakeRequest) {
    OpsTeamWorkflow workflow = workflowClient.newWorkflowStub(
            OpsTeamWorkflow.class,"ops-" + intakeRequest.getRequestId()
    );

    workflow.cancelWork();  // Signal call
    return "🚨 Cancel signal sent to OpsTeamWorkflow";
}
```

### ✅ Output (Logs):
```
🚀 [LoggingWorkflowClientInterceptor] Starting workflow: intake-12sws27-1001
📥 [LoggingWorkflowInboundInterceptor:init] Initialized for Workflow ID: intake-12sws27-1001
🚀 [LoggingWorkflowInboundInterceptor:execute] Starting workflow: IntakeWorkflow
📥 Intake received for: 12sws27-1001
🔧 [LoggingActivityInterceptor:init] Initialized for activity: CheckZipService
🚀 [LoggingActivityInterceptor:execute] Starting activity: CheckZipService
✅ Checking service availability for ZIP: 75001
✅ [LoggingActivityInterceptor:execute] Finished activity: CheckZipService
🔧 [LoggingActivityInterceptor:init] Initialized for activity: ValidateAddress
🚀 [LoggingActivityInterceptor:execute] Starting activity: ValidateAddress
📍 Validating address: 123 Main Street
✅ [LoggingActivityInterceptor:execute] Finished activity: ValidateAddress
🔧 [LoggingActivityInterceptor:init] Initialized for activity: ValidateAddress1
🚀 [LoggingActivityInterceptor:execute] Starting activity: ValidateAddress1
📍 Validating1 address: 123 Main Street
✅ [LoggingActivityInterceptor:execute] Finished activity: ValidateAddress1
🔧 [LoggingActivityInterceptor:init] Initialized for activity: CheckBandwidth
🚀 [LoggingActivityInterceptor:execute] Starting activity: CheckBandwidth
🔌 Checking bandwidth availability for 100 Mbps
✅ [LoggingActivityInterceptor:execute] Finished activity: CheckBandwidth
✅ All checks passed. 📋 Waiting for manager approval...
📩 [LoggingWorkflowClientInterceptor] Sending signal to: intake-12sws27-1001
📨 [LoggingWorkflowInboundInterceptor:signal] Received signal: approveInstallation
📩 Received manager approval email:12sws27-1001
✅ Manager approved. Triggering InstallationWorkflow...
📥 [LoggingWorkflowInboundInterceptor:init] Initialized for Workflow ID: 2fd94638-4a37-3d4b-b5b4-e1cdee7c701f
🚀 [LoggingWorkflowInboundInterceptor:execute] Starting workflow: InstallationWorkflow
🚀 [InstallationWorkflow] Starting for ZIP: 75001, Address: 123 Main Street
📥 [LoggingWorkflowInboundInterceptor:init] Initialized for Workflow ID: ops-12sws27-1001
🚀 [LoggingWorkflowInboundInterceptor:execute] Starting workflow: OpsTeamWorkflow
🔧 [OpsTeam] Starting installation
🔧 [OpsTeam] Installing service at ZIP: 75001, Address: 123 Main Street
👨‍🔧 Starting OpsTeam Activities at user location...
🔧 [LoggingActivityInterceptor:init] Initialized for activity: ReachUserLocation
🚀 [LoggingActivityInterceptor:execute] Starting activity: ReachUserLocation
🚗 [OpsTeam] Reaching user location at ZIP: 75001, Address: 123 Main Street
✅ [LoggingActivityInterceptor:execute] Finished activity: ReachUserLocation
📩 [LoggingWorkflowClientInterceptor] Sending signal to: ops-12sws27-1001
📨 [LoggingWorkflowInboundInterceptor:signal] Received signal: cancelWork
🚨 [OpsTeam] Received cancel signal
❌ [OpsTeam] Cancelled after reaching location
✅ [LoggingWorkflowInboundInterceptor:execute] Completed workflow: OpsTeamWorkflow
✅ [InstallationWorkflow] Installation steps completed.
✅ [LoggingWorkflowInboundInterceptor:execute] Completed workflow: InstallationWorkflow
✅ [LoggingWorkflowInboundInterceptor:execute] Completed workflow: IntakeWorkflow
```

---

## 🐞 Common Errors We Solved

| Error | Cause | Fix |
|------|-------|------|
| `ALREADY_EXISTS` | Workflow with same ID already running | Use `workflowStub.signalWithStart(...)` or handle gracefully |
| `NullPointerException at WorkflowStub.start()` | Untyped stub was null | Ensure you create typed stub with correct WorkflowOptions |

---

## ✅ What You Learned Today

- 🔍 How to introspect workflows with queries
- 🧠 How to inject custom logic using Interceptors
- ❤️ How to monitor and cancel activities using Heartbeats and Signals

---

# 📘 Day 6: Multi-Phase Telecom Workflow – Provisioning, Billing, and Support Setup

✅ This builds upon your existing telecom flow:  
- `IntakeWorkflow` handles pre-checks and approval.
- `InstallationWorkflow` installs services via `OpsTeamWorkflow`.
- Now we extend the flow with **3 new child workflows**:

---

## 🧩 Updated Execution Flow

```
IntakeWorkflow
   ├─ InstallationWorkflow
   │    └─ OpsTeamWorkflow
   │        ├─ reachUserLocation()
   │        ├─ installService()
   │        └─ notifyUser()
   ├─ ProvisioningWorkflow
   │    ├─ provisionInternet()
   │    └─ provisionVoIP()
   ├─ BillingWorkflow
   │    ├─ setupBilling()
   │    └─ sendInvoice()
   └─ SupportSetupWorkflow
        ├─ registerSupportProfile()
        └─ sendWelcomeKit()
```

---

## 📦 Updated IntakeWorkflowImpl.java

```java
public class IntakeWorkflowImpl implements IntakeWorkflow {
    private boolean approved = false;
    private String status = "Not started";

    private final IntakeActivity intakeActivity = Workflow.newActivityStub(
        IntakeActivity.class,
        ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(10)).build()
    );

    @Override
    public void start(IntakeRequest intakeRequest) {
        status = "🏃‍♂️Running pre-checks...";
        boolean serviceAvailable = intakeActivity.checkZipService(intakeRequest.getZip());
        boolean addressValid = intakeActivity.validateAddress(intakeRequest.getAddress());

        int version = Workflow.getVersion("validateMetroAddress", Workflow.DEFAULT_VERSION, 1);
        if (version == 1) {
            boolean metroAddress = intakeActivity.validateMetroAddress(intakeRequest.getAddress());
            if (!metroAddress) {
                status = "❌ Metro validation failed";
                return;
            }
        }

        boolean bandwidthOk = intakeActivity.checkBandwidth(intakeRequest.getZip(), intakeRequest.getMbps());

        if (!serviceAvailable || !addressValid || !bandwidthOk) {
            status = "❌ Pre-checks failed";
            return;
        }

        status = "Waiting for manager approval...👮‍♂️⏱️";
        Workflow.await(() -> approved);

        // ✅ Trigger installation
        status = "👮‍♂️ Approved. Triggering InstallationWorkflow...";
        Workflow.newChildWorkflowStub(InstallationWorkflow.class)
                .startInstallation(intakeRequest);
        status = "Installation complete 🚀";

        // ✅ Fan-out new child workflows
        ChildWorkflowOptions postInstallOptions = ChildWorkflowOptions.newBuilder()
                .setTaskQueue(TaskQueue.Name.POST_INSTALL_TASK_QUEUE)
                .setWorkflowRunTimeout(Duration.ofMinutes(10))
                .build();

        Workflow.newChildWorkflowStub(ProvisioningWorkflow.class, postInstallOptions)
                .startProvisioning(intakeRequest);

        Workflow.newChildWorkflowStub(BillingWorkflow.class, postInstallOptions)
                .startBilling(intakeRequest);

        Workflow.newChildWorkflowStub(SupportSetupWorkflow.class, postInstallOptions)
                .startSupportSetup(intakeRequest);

        status = "✅ Post-install workflows completed 🧩";
    }

    @Override
    public void approveInstallation(IntakeRequest intakeRequest) {
        approved = true;
        status = "📩 Received manager approval email";
    }

    @Override
    public String findCurrentStatus() {
        return status;
    }
}
```

---

## 🔧 PostInstallActivity.java

```java
@ActivityInterface
public interface PostInstallActivity {
    @ActivityMethod void provisionInternet(IntakeRequest request);
    @ActivityMethod void provisionVoIP(IntakeRequest request);
    @ActivityMethod void setupBilling(IntakeRequest request);
    @ActivityMethod void sendInvoice(IntakeRequest request);
    @ActivityMethod void registerSupportProfile(IntakeRequest request);
    @ActivityMethod void sendWelcomeKit(IntakeRequest request);

    static PostInstallActivity newStub() {
        return Workflow.newActivityStub(PostInstallActivity.class,
            ActivityOptions.newBuilder()
                .setTaskQueue(TaskQueue.Name.POST_INSTALL_TASK_QUEUE)
                .setStartToCloseTimeout(Duration.ofSeconds(10))
                .build());
    }
}
```

---

## ✅ Sample Workflow Implementation (ProvisioningWorkflow)

```java
@WorkflowInterface
public interface ProvisioningWorkflow {
    @WorkflowMethod
    void startProvisioning(IntakeRequest intakeRequest);
}
```

```java
public class ProvisioningWorkflowImpl implements ProvisioningWorkflow {
    private final PostInstallActivity activity = PostInstallActivity.newStub();

    @Override
    public void startProvisioning(IntakeRequest intakeRequest) {
        System.out.println("📦 [ProvisioningWorkflow] Starting...");
        activity.provisionInternet(intakeRequest);
        activity.provisionVoIP(intakeRequest);
        System.out.println("✅ [ProvisioningWorkflow] Done.");
    }
}
```

---

## 🗂️ Register in Worker Config

```java
Worker postInstallWorker = factory.newWorker(TaskQueue.Name.POST_INSTALL_TASK_QUEUE);
postInstallWorker.registerWorkflowImplementationTypes(
    ProvisioningWorkflowImpl.class,
    BillingWorkflowImpl.class,
    SupportSetupWorkflowImpl.class
);
postInstallWorker.registerActivitiesImplementations(new PostInstallActivityImpl());
```

---

## 🗄️ Dummy DB for dbId-based Lookup

```java
@Service
public class DummyDb {
    private final Map<String, List<DummyDbEntity>> userTables = new HashMap<>();

    public int insert(String tableName, DummyDbEntity tableData) {
        userTables.computeIfAbsent(tableName, k -> new ArrayList<>()).add(tableData);
        return 1;
    }

    public DummyDbEntity findById(String tableName, String id) {
        return userTables.getOrDefault(tableName, List.of())
                         .stream().filter(e -> e.id().equals(id)).findFirst().orElse(null);
    }
}
```

---

## ✅ Controller APIs using dbId

```java
@PostMapping("/start")
public IntakeResponse start(@RequestBody IntakeRequest intakeRequest) {
    IntakeWorkflow workflow = workflowClient.newWorkflowStub(
        IntakeWorkflow.class,
        WorkflowOptions.newBuilder().setTaskQueue(TaskQueue.Name.INTAKE_TASK_QUEUE).build()
    );
    WorkflowExecution execution = WorkflowClient.start(workflow::start, intakeRequest);
    WorkflowDbEntity dbEntity = workflowDbService.save(intakeRequest, execution);
    return IntakeResponse.builder().dbId(dbEntity.getDbId()).build();
}

@PostMapping("/approve")
public String approve(@RequestBody WorkflowRequest req) {
    WorkflowDbEntity dbEntity = workflowDbService.findWorkflowId(req.getDbId());
    workflowClient.newWorkflowStub(IntakeWorkflow.class, dbEntity.getWorkflowId())
                  .approveInstallation(dbEntity.getIntakeRequest());
    return "✅ Approved: " + dbEntity.getWorkflowId();
}

@PostMapping("/status")
public String status(@RequestBody WorkflowRequest req) {
    WorkflowDbEntity dbEntity = workflowDbService.findWorkflowId(req.getDbId());
    return workflowClient.newWorkflowStub(IntakeWorkflow.class, dbEntity.getWorkflowId())
                         .findCurrentStatus();
}
```

---

## 🧠 Summary of Concepts in Day 6

| Concept                  | Description                                                                 |
|--------------------------|-----------------------------------------------------------------------------|
| Child Workflow Fan-out   | Launched Provisioning, Billing, Support flows in parallel                   |
| Activity Sharing         | All post-install workflows used a single `PostInstallActivity` stub         |
| Dummy Database           | Stored `workflowId` against a generated `dbId`                              |
| Query Support            | `/status` endpoint reads workflow status using `@QueryMethod`               |
| Approval Flow            | `/approve` endpoint triggers approval signal                                |
