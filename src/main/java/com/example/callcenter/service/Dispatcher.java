package com.example.callcenter.service;

import com.example.callcenter.model.Employee;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface Dispatcher {

    Mono<Employee> dispatchCall();

    Flux<Employee> checkStatus();
}
