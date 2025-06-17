package com.salilvnair.temporal.config;

import com.salilvnair.temporal.activity.HelloActivityImpl;
import com.salilvnair.temporal.activity.OpsTeamActivityImpl;
import com.salilvnair.temporal.activity.SiteCheckActivityImpl;
import com.salilvnair.temporal.type.TaskQueue;
import com.salilvnair.temporal.workflow.*;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class TemporalFactoryStarter {

    private final WorkerFactory factory;

    public TemporalFactoryStarter(WorkerFactory factory) {
        this.factory = factory;
    }

    @PostConstruct
    public void startFactoryAndRegisterWorkers() {
        // Register helloWorker
        Worker helloWorker = factory.newWorker(TaskQueue.Name.HELLO_TASK_QUEUE);
        helloWorker.registerWorkflowImplementationTypes(HelloWorkflowImpl.class);
        helloWorker.registerActivitiesImplementations(new HelloActivityImpl());

        // Register intakeWorker
        Worker intakeWorker = factory.newWorker(TaskQueue.Name.INTAKE_TASK_QUEUE);
        intakeWorker.registerWorkflowImplementationTypes(IntakeWorkflowImpl.class);
        intakeWorker.registerWorkflowImplementationTypes(InstallationWorkflowImpl.class);
        intakeWorker.registerActivitiesImplementations(new SiteCheckActivityImpl());


        Worker opsWorker = factory.newWorker(TaskQueue.Name.OPS_TASK_QUEUE);
        opsWorker.registerWorkflowImplementationTypes(OpsTeamWorkflowImpl.class);
        opsWorker.registerActivitiesImplementations(new OpsTeamActivityImpl());

        factory.start(); // ✅ Only start after all registrations
        System.out.println("✅ Temporal workers registered and factory started");
    }
}
