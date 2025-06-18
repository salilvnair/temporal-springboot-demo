package com.salilvnair.temporal.activity;


import io.temporal.activity.Activity;
import io.temporal.activity.ActivityExecutionContext;
import io.temporal.failure.CanceledFailure;

public class OpsTeamActivityImpl implements OpsTeamActivity {
    @Override
    public void reachUserLocation(String zip, String address) {
        System.out.println("🚗 [OpsTeam] Reaching user location at ZIP: " + zip + ", Address: " + address);
        // Simulate delay
        try {
            Thread.sleep(1000); // In real workflows, use Workflow.sleep() if inside a workflow
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

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

    @Override
    public void verifyCableConnection(String zip, String address) {
        ActivityExecutionContext context = Activity.getExecutionContext();

        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during cable verification", e);
        }
    }

    @Override
    public void notifyUser(String email) {
        System.out.println("📧 [OpsTeam] Sending installation complete email to: " + email);
        try {
            Thread.sleep(500);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
