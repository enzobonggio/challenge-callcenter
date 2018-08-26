package com.example.callcenter.model;

public enum EmployeeType {
    OPERATOR(1),
    SUPERVISOR(2),
    DIRECTOR(3);

    private int priority;

    EmployeeType(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}