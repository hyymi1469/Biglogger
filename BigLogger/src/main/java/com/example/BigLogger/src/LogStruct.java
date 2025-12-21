package com.example.BigLogger.src;

import java.util.ArrayList;
import java.util.List;

public class LogStruct {
    public String name;
    public Integer version;
    public Integer filter;
    public String desc;

    public List<LogEntry> entries = new ArrayList<>();

    @Override
    public String toString() {
        return "LogStruct{" +
                "name='" + name + '\'' +
                ", version=" + version +
                ", filter=" + filter +
                ", desc='" + desc + '\'' +
                ", entries=" + entries +
                '}';
    }
}