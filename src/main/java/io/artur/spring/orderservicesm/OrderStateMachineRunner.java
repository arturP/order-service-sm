package io.artur.spring.orderservicesm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.state.State;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

/**
 *
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OrderStateMachineRunner implements ApplicationRunner {

    private final StateMachineFactory<OrderStates, OrderEvents> factory;
    private final OrderService orderService;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        dbBasedStateTransitions();
    }

    private void dbBasedStateTransitions() {
        Order order = orderService.create(new Date());

        StateMachine<OrderStates, OrderEvents> stateMachine = orderService.pay(order.getId(), UUID.randomUUID().toString());
        log.info("After paying state: " + stateMachine.getState().getId().name());
        log.info("order: " + orderService.getOrder(order.getId()));

        stateMachine = orderService.fulfill(order.getId(), UUID.randomUUID().toString());
        log.info("After fulfilling state: " + stateMachine.getState().getId().name());
        log.info("order: " + orderService.getOrder(order.getId()));
    }

    private void inMemoryStateTransitions() {
        Long orderId = 2211098L;
        StateMachine<OrderStates, OrderEvents> stateMachine = this.factory.getStateMachine(orderId.toString());
        stateMachine.getExtendedState().getVariables().putIfAbsent("orderId", orderId);
        stateMachine.start();
        log.info("Current state: " + stateMachine.getState().getId().name());
        stateMachine.sendEvent(OrderEvents.PAY);
        log.info("Current state: " + stateMachine.getState().getId().name());
        stateMachine.sendEvent(MessageBuilder.withPayload(OrderEvents.FULFILL).build());
        log.info("Current state: " + stateMachine.getState().getId().name());
    }
}
