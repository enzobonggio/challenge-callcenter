package com.example.callcenter.service.impl;

import com.example.callcenter.exception.OutOfEmployeeException;
import com.example.callcenter.helper.RandomHelper;
import com.example.callcenter.model.Employee;
import com.example.callcenter.service.Dispatcher;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.RejectedExecutionException;
import java.util.function.Function;

@Service
@Slf4j
public class DispatcherImpl implements Dispatcher {

    private final int from;
    private final int to;
    private final EmployeeQueue employees;
    private final Scheduler scheduler;

    public DispatcherImpl(
            @Value("${callcenter.duration.from:5}") int from,
            @Value("${callcenter.duration.to:10}") int to,
            @Qualifier("callcenter-scheduler") Scheduler scheduler,
            EmployeeQueue employees) {
        this.employees = employees;
        this.from = from;
        this.to = to;
        this.scheduler = scheduler;
    }


    /**
     * Dispatch a call for some employee so that they can take it,
     * if there are no more space we drop the call and send an error
     *
     * @return The employee that took the call
     */
    @Override
    public Mono<Employee> dispatchCall() {
        return Mono.defer(() -> Mono.justOrEmpty(employees.poll()))
                .switchIfEmpty(Mono.error(new OutOfEmployeeException("There is no employee to take the call")))
                .flatMap(this::takeCall)
                .map(employees::add)
                .subscribeOn(scheduler)
                .publishOn(Schedulers.single())
                .onErrorMap(RejectedExecutionException.class, handleRejectedExecution());
    }

    /**
     * Check the status of the employees that can take a call
     *
     * @return The Flux of employees that can take a call
     */
    @Override
    public Flux<Employee> checkStatus() {
        return Flux.defer(() -> Flux.fromIterable(employees.getEmployees()));
    }

    /**
     * Handles the error that Executor/ Scheduler drops when there is no more space to take a call
     *
     * @return
     */
    private Function<RejectedExecutionException, Throwable> handleRejectedExecution() {
        return ex -> {
            log.error("The executor cannot handle this request");
            return new OutOfEmployeeException(ex);
        };
    }

    /**
     * Given an employee we now attend the call
     *
     * @param employee The employee that will take the call
     * @return
     */
    private Mono<Employee> takeCall(Employee employee) {
        val seconds = RandomHelper.nextInt(from, to);
        return Mono.fromCallable(() -> sleepAndReturnEmployee(employee, seconds))
                .doOnSubscribe(s -> log.info("Employee {} will be attending a call for {} seconds", employee, seconds))
                .doOnError(e -> log.error("There was an error doing blocking activity we will return employee to the queue {}", employee))
                .onErrorReturn(employee);
    }

    /**
     * Here is the blocking action
     *
     * @param employee The employee that is taking the call
     * @param seconds  quantity of senconds that the thread will we unusable
     * @return The employee that took the call
     * @throws InterruptedException if there is an error on the thread
     */
    private Employee sleepAndReturnEmployee(Employee employee, int seconds) throws InterruptedException {
        Thread.sleep(seconds * 1000L);
        return employee;
    }
}
