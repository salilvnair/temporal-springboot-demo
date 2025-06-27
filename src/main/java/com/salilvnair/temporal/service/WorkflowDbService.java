package com.salilvnair.temporal.service;

import com.salilvnair.temporal.db.DummyDb;
import com.salilvnair.temporal.entity.WorkflowDbEntity;
import com.salilvnair.temporal.model.IntakeRequest;
import io.temporal.api.common.v1.WorkflowExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class WorkflowDbService {

    @Autowired
    private DummyDb db;

    public WorkflowDbEntity save(IntakeRequest intakeRequest, WorkflowExecution workflowExecution) {
        WorkflowDbEntity dbEntity = WorkflowDbEntity
                                        .builder()
                                        .workflowId(workflowExecution.getWorkflowId())
                                        .intakeRequest(intakeRequest)
                                        .dbId(UUID.randomUUID().toString())
                                        .build();
        db.insert("WorkflowDb", dbEntity);
        return dbEntity;
    }

    public WorkflowDbEntity findWorkflowId(String dbId) {
        return (WorkflowDbEntity) db.findById("WorkflowDb", dbId);
    }


}
