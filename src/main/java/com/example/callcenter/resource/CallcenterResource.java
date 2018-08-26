package com.example.callcenter.resource;

import com.example.callcenter.model.CallBody;
import com.example.callcenter.model.Employee;
import com.example.callcenter.service.Dispatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController("/callcenter")
@Slf4j
public class CallcenterResource {

    private final Dispatcher dispatcher;

    @Autowired
    public CallcenterResource(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @GetMapping
    public Flux<Employee> callcenterStatus() {
        return dispatcher.checkStatus();
    }

    @PostMapping
    public Mono<Employee> dispatchCall(@RequestBody CallBody body) {
        return dispatcher.dispatchCall()
                .doOnSubscribe(s -> log.info("Dispatch call with UUID: {}", body.getEvento()))
                .doOnNext(employee -> log.info("The employee with UUID {} took the call with UUID {}", body.getEvento()));
    }
}
