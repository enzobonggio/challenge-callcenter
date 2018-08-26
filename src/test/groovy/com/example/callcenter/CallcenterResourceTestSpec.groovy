package com.example.callcenter

import com.example.callcenter.exception.OutOfEmployeeException
import com.example.callcenter.model.Operator
import com.example.callcenter.service.Dispatcher
import reactor.core.publisher.Mono
import spock.lang.Specification

class CallcenterResourceTestSpec extends Specification {

    def dispatcherOK = Mock(Dispatcher) {
        dispatchCall() >> Mono.just(Operator.of())
    }

    def dispatcherError = Mock(Dispatcher) {
        dispatchCall() >> Mono.error(new OutOfEmployeeException("Error"))
    }

    def "test dispatch call ok"() {

    }

    def "test dispatch call error"() {

    }
}
