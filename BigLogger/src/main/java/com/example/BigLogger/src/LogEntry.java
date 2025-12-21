package com.example.BigLogger.src;

public class LogEntry {
    public String name;
    public String type;
    public Integer size;          // optional
    public Integer index;         // optional
    public String defaultValue;   // optional
    public String desc;           // optional

    @Override
    public String toString() {
        return "LogEntry{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", size=" + size +
                ", index=" + index +
                ", defaultValue='" + defaultValue + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }
}