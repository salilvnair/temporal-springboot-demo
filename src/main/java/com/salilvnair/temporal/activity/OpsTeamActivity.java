package com.salilvnair.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface OpsTeamActivity {
    @ActivityMethod
    void reachUserLocation(String zip, String address);
    @ActivityMethod
    void installedService(String zip, String address);

    @ActivityMethod
    void verifyCableConnection(String zip, String address);

    @ActivityMethod
    void notifyUser(String email);
}
