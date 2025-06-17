package com.salilvnair.temporal.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IntakeRequest {
    private String requestId;
    private String zip;
    private String address;
    private int mbps;
    private String userEmailId;
}
