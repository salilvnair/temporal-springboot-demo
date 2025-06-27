package com.salilvnair.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface IntakeActivity {
    @ActivityMethod
    boolean checkZipService(String zipCode);
    @ActivityMethod
    boolean validateAddress(String address);
    @ActivityMethod
    boolean validateMetroAddress(String address);
    @ActivityMethod
    boolean checkBandwidth(String zipCode, int requestedMbps);
}
