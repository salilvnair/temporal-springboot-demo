package com.salilvnair.temporal.workflow;

import com.salilvnair.temporal.activity.OpsTeamActivity;
import com.salilvnair.temporal.model.IntakeRequest;
import com.salilvnair.temporal.type.TaskQueue;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import java.time.Duration;

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
