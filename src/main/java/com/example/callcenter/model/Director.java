package com.example.callcenter.model;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value(staticConstructor = "of")
@EqualsAndHashCode(callSuper = true)
public class Director extends Employee {


    @Override
    public EmployeeType getType() {
        return EmployeeType.DIRECTOR;
    }

}
