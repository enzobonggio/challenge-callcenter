package com.example.callcenter.service.impl;

import com.example.callcenter.model.Director;
import com.example.callcenter.model.Employee;
import com.example.callcenter.model.Operator;
import com.example.callcenter.model.Supervisor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@Slf4j
public class EmployeeQueue {

    private static final int ZERO = 0;

    private final BlockingQueue<Employee> queue;

    public EmployeeQueue(
            @Value("${callcenter.employees.operator:7}") int operatorsNumber,
            @Value("${callcenter.employees.supervisor:2}") int supervisorsNumber,
            @Value("${callcenter.employees.director:1}") int directorsNumber) {
        queue = new PriorityBlockingQueue<>();
        addEmployees(operatorsNumber, supervisorsNumber, directorsNumber);
    }

    /**
     * Initial filling of the queue
     *
     * @param operatorsNumber
     * @param supervisorsNumber
     * @param directorsNumber
     */
    private void addEmployees(int operatorsNumber, int supervisorsNumber, int directorsNumber) {
        val operators = IntStream.range(ZERO, operatorsNumber).mapToObj(idx -> Operator.of()).collect(Collectors.toList());
        val supervisors = IntStream.range(ZERO, supervisorsNumber).mapToObj(idx -> Supervisor.of()).collect(Collectors.toList());
        val directors = IntStream.range(ZERO, directorsNumber).mapToObj(idx -> Director.of()).collect(Collectors.toList());
        queue.addAll(operators);
        queue.addAll(supervisors);
        queue.addAll(directors);
    }

    /**
     * Get the employees that are currently available to take a call
     *
     * @return
     */
    BlockingQueue<Employee> getEmployees() {
        return queue;
    }

    /**
     * Get one employee from the queue taking in account the priority of them
     *
     * @return
     */
    Employee poll() {
        val employee = queue.poll();
        log.info("We are polling one employee from the queue: {}", employee);
        return employee;
    }

    /**
     * Adding one employee back to the queue after finishing the call
     *
     * @param employee
     * @return
     */
    Employee add(Employee employee) {
        log.info("Adding one employee on the queue: {}", employee);
        queue.add(employee);
        return employee;
    }
}
