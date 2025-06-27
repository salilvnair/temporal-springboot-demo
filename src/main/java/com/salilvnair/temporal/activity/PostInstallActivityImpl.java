package com.salilvnair.temporal.activity;

import com.salilvnair.temporal.model.IntakeRequest;

public class PostInstallActivityImpl implements PostInstallActivity {
    @Override
    public void provisionInternet(IntakeRequest request) {
        System.out.println("🌐 Provisioning Internet for " + request.getAddress());
    }

    @Override
    public void provisionVoIP(IntakeRequest request) {
        System.out.println("📞 Provisioning VoIP for " + request.getAddress());
    }

    @Override
    public void setupBilling(IntakeRequest request) {
        System.out.println("💰 Setting up billing for " + request.getUserEmailId());
    }

    @Override
    public void sendInvoice(IntakeRequest request) {
        System.out.println("📨 Sending invoice to " + request.getUserEmailId());
    }

    @Override
    public void registerSupportProfile(IntakeRequest request) {
        System.out.println("🛠️ Registering support profile for " + request.getUserEmailId());
    }

    @Override
    public void sendWelcomeKit(IntakeRequest request) {
        System.out.println("🎁 Sending welcome kit to " + request.getUserEmailId());
    }
}
