package com.example.callcenter

import com.example.callcenter.config.AppConfig
import com.example.callcenter.exception.OutOfEmployeeException
import com.example.callcenter.model.Director
import com.example.callcenter.model.Employee
import com.example.callcenter.model.Operator
import com.example.callcenter.model.Supervisor
import com.example.callcenter.service.Dispatcher
import com.example.callcenter.service.impl.DispatcherImpl
import com.example.callcenter.service.impl.EmployeeQueue
import org.mockito.Mockito
import reactor.core.publisher.Flux
import reactor.core.publisher.Hooks
import reactor.core.publisher.Mono
import reactor.core.publisher.ParallelFlux
import reactor.core.scheduler.Schedulers
import reactor.test.StepVerifier
import spock.lang.Shared
import spock.lang.Specification

import java.time.Duration
import java.util.stream.DoubleStream

class DispatcherTestSpec extends Specification {

    public static final String OPERATOR = "operator"
    public static final String SUPERVISOR = "supervisor"
    public static final String DIRECTOR = "director"
    public static final String ERROR = "error"
    public static final int POOL_CORE = 10
    public static final int POOL_MAX = 10
    public static final String POOL_PREFIX = "callcenter-test"
    public static final int POOL_QUEUE = 0
    public static final int MIN_CALL_DURATION = 5
    public static final int MAX_CALL_DURATION = 10
    @Shared
    int numberOfOperators = 7
    @Shared
    int numberOfSupervisors = 2
    @Shared
    int numberOfDirectors = 1
    @Shared
    int extraCalls = 1

    def verifyStreategy = [
            "operator"  : expectOperator,
            "supervisor": expectSupervisor,
            "director"  : expectDirector,
            "error"     : expectError]

    def appConfig = new AppConfig()

    def employeeQueue = new EmployeeQueue(numberOfOperators, numberOfSupervisors, numberOfDirectors)

    def setup() {
        Hooks.onOperatorDebug()
    }


    def "calling for #times times at the same time"(int times, String strategy) {
        given:
        def executor = appConfig.executorCallcenter(POOL_CORE, POOL_QUEUE, POOL_MAX, POOL_PREFIX)
        def scheduler = appConfig.schedulerCallcenter(executor)
        def dispatcher = new DispatcherImpl(MIN_CALL_DURATION, MAX_CALL_DURATION, scheduler, employeeQueue)

        when:
        def result = Flux.range(0, times)
                .parallel().runOn(Schedulers.elastic())
                .flatMap { dispatchEitherCall(dispatcher) }
        then:
        assert verifyStreategy[strategy](result, times) <= Duration.ofSeconds(MAX_CALL_DURATION)

        where:
        times                                                                    | strategy
        numberOfOperators                                                        | OPERATOR
        numberOfOperators + numberOfSupervisors                                  | SUPERVISOR
        numberOfOperators + numberOfSupervisors + numberOfDirectors              | DIRECTOR
        numberOfOperators + numberOfSupervisors + numberOfDirectors + extraCalls | ERROR
    }

    def "simulate calling using a poison distribution"(int quantityOfTimes, int quantityOfMinutes, int testTimeInMinutes, int waitOnPool) {
        given:
        def randomGen = DoubleStream.generate { Math.random() }
        def nextTime = randomGen.map { -Math.log(1.0 - it) / (quantityOfTimes / quantityOfMinutes as Double) }
        def executor = appConfig.executorCallcenter(POOL_CORE, waitOnPool, POOL_MAX, POOL_PREFIX)
        def scheduler = appConfig.schedulerCallcenter(executor)
        def dispatcher = Mockito.spy(new DispatcherImpl(MIN_CALL_DURATION, MAX_CALL_DURATION, scheduler, employeeQueue))

        when:
        def result = Flux.fromStream { nextTime.boxed() }
                .map { it * 60 }
                .map { it as Long }
                .doOnNext { println("Will consume next in $it seconds") }
                .doOnNext { Mono.delay(Duration.ofSeconds(it)).block() }
                .flatMap { dispatchEitherCall(dispatcher) }
                .take(Duration.ofMinutes(testTimeInMinutes))
                .collectList()
                .block()

        def callsByEmployee = result.findAll { it.isLeft() }.collect { it.left }.groupBy {
            if (it instanceof Operator) return OPERATOR
            else if (it instanceof Supervisor) return SUPERVISOR
            else if (it instanceof Director) return DIRECTOR
            throw new RuntimeException("Error on the collect")
        }

        println "--- Simulation is over ---"
        println "Calls that we could not attend: ${result.findAll { it.isRight() }.size()}"
        println "Calls that were attended by an Operator ${callsByEmployee[OPERATOR].size()}"
        println "Calls that were attended by a Supervisor ${callsByEmployee[SUPERVISOR].size()}"
        println "Calls that were attended by a Director ${callsByEmployee[DIRECTOR].size()}"

        then:
        //We are doing this cause .take() will not terminate each and every call
        Mockito.verify(dispatcher, Mockito.atLeast(result.size() - POOL_MAX)).dispatchCall()

        where:
        quantityOfTimes | quantityOfMinutes | testTimeInMinutes | waitOnPool
        40              | 1                 | 1                 | 0
        40              | 1                 | 1                 | 5
    }

    def dispatchEitherCall(Dispatcher dispatcher) {
        dispatcher.dispatchCall()
                .map { Either.ofLeft(it) }
                .onErrorResume { Mono.just(Either.ofRight(it)) }
    }

    def expectOperator = { ParallelFlux<Either<Employee, RuntimeException>> result, int times ->
        StepVerifier.create(result.sequential().filter { it.isLeft() }.map { it.left }.collectList())
                .expectNextMatches {
            numberOfOperators == it.size()
            numberOfOperators == it.findAll { i -> i instanceof Operator }.size()
        }.verifyComplete()
    }

    def expectSupervisor = { ParallelFlux<Either<Employee, RuntimeException>> result, int times ->
        StepVerifier.create(result.sequential().filter { it.isLeft() }.map { it.left }.collectList())
                .expectNextMatches {
            numberOfOperators + numberOfSupervisors == it.size()
            numberOfOperators == it.findAll { i -> i instanceof Operator }.size()
            numberOfSupervisors == it.findAll { i -> i instanceof Supervisor }.size()
        }.verifyComplete()
    }

    def expectDirector = { ParallelFlux<Either<Employee, RuntimeException>> result, int times ->
        StepVerifier.create(result.sequential().filter { it.isLeft() }.map { it.left }.collectList())
                .expectNextMatches {
            numberOfOperators + numberOfSupervisors + numberOfDirectors == it.size()
            numberOfOperators == it.findAll { i -> i instanceof Operator }.size()
            numberOfSupervisors == it.findAll { i -> i instanceof Supervisor }.size()
            numberOfDirectors == it.findAll { i -> i instanceof Director }.size()
        }.verifyComplete()
    }

    def expectError = { ParallelFlux<Either<Employee, RuntimeException>> result, int times ->
        StepVerifier.create(result.sequential().filter { it.isRight() }.map { it.right }.collectList())
                .expectNextMatches {
            extraCalls == it.size()
            it.size() == it.findAll { i -> i instanceof OutOfEmployeeException }.size()
        }.verifyComplete()
    }

    static class Either<L, R> {

        final L left
        final R right

        Either(L left, R right) {
            this.left = left
            this.right = right
        }

        static <L, R> Either<L, R> ofLeft(L left) {
            new Either(left, null)
        }

        static <L, R> Either<L, R> ofRight(right) {
            new Either(null, right)
        }

        boolean isLeft() {
            return Objects.nonNull(left)
        }

        boolean isRight() {
            return Objects.nonNull(right)
        }

    }
}
