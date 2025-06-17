package com.salilvnair.temporal.activity;


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
        System.out.println("🚗 [OpsTeam] Installed Service at user location ZIP: " + zip + ", Address: " + address);
        // Simulate delay
        try {
            Thread.sleep(1000); // In real workflows, use Workflow.sleep() if inside a workflow
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void verifyCableConnection(String zip, String address) {
        System.out.println("🧪 [OpsTeam] Verifying cable connection at ZIP: " + zip + ", Address: " + address);
        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
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
