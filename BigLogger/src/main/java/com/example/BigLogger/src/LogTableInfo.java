package com.example.BigLogger.src;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class LogTableInfo {

    // Thread-safe
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<String>> tableInfoMap = new ConcurrentHashMap<>();

    // 싱글톤
    private LogTableInfo() {}

    // Singleton Holder
    private static class Holder {
        private static final LogTableInfo INSTANCE = new LogTableInfo();
    }

    public static LogTableInfo getInstance() {
        return Holder.INSTANCE;
    }

    // Map getter
    public ConcurrentHashMap<String, CopyOnWriteArrayList<String>> GetLogNameMap() {
        return tableInfoMap;
    }

    // Retrieve list
    public CopyOnWriteArrayList<String> GetList(String key) {
        return tableInfoMap.get(key);
    }
}