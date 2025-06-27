package com.salilvnair.temporal.entity;

import com.salilvnair.temporal.db.DummyDbEntity;
import com.salilvnair.temporal.model.IntakeRequest;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkflowDbEntity implements DummyDbEntity {
    private String dbId;
    private String workflowId;
    private IntakeRequest intakeRequest;

    @Override
    public String id() {
        return dbId;
    }
}
