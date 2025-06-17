package com.salilvnair.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface SiteCheckActivity {
    @ActivityMethod
    boolean checkZipService(String zipCode);
    @ActivityMethod
    boolean validateAddress(String address);
    @ActivityMethod
    boolean checkBandwidth(String zipCode, int requestedMbps);
}
