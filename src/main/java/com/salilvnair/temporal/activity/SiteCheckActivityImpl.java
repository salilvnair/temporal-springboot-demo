package com.salilvnair.temporal.activity;

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
    public boolean validateAddress1(String address) {
        System.out.println("📍 Validating1 address: " + address);
        return address != null && address.length() > 5;
    }

    @Override
    public boolean checkBandwidth(String zipCode, int requestedMbps) {
        System.out.println("🔌 Checking bandwidth availability for " + requestedMbps + " Mbps");
        return requestedMbps <= 500;
    }
}
