package com.salilvnair.temporal.workflow;

import com.salilvnair.temporal.activity.OpsTeamActivity;
import com.salilvnair.temporal.model.IntakeRequest;
import com.salilvnair.temporal.type.TaskQueue;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import java.time.Duration;

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
