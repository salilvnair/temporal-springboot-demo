package com.salilvnair.temporal.db;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class DummyDb {

    private final Map<String, List<DummyDbEntity>> userTables = new HashMap<>();


    public  int insert(String tableName, DummyDbEntity tableData) {
        List<DummyDbEntity> entries = userTables.getOrDefault(tableName, new ArrayList<>());
        entries.add(tableData);
        userTables.put(tableName, entries);
        return 1;
    }

    public DummyDbEntity findById(String tableName, String id) {
        List<DummyDbEntity> objects = userTables.get(tableName);
        return objects.stream().filter(obj -> obj.id().equals(id)).toList().get(0);
    }

}
