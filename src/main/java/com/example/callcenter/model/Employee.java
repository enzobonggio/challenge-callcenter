package com.example.callcenter.model;

import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@ToString
public abstract class Employee implements Comparable<Employee> {

    @Getter
    protected UUID uuid;

    Employee() {
        this.uuid = UUID.randomUUID();
    }

    public abstract EmployeeType getType();

    @Override
    public int compareTo(Employee var1) {
        return Integer.compare(this.getType().getPriority(), var1.getType().getPriority());
    }

}