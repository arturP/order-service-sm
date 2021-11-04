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

/**
 *
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OrderStateMachineRunner implements ApplicationRunner {

    private final StateMachineFactory<OrderStates, OrderEvents> factory;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String orderId = "2211098";
        StateMachine<OrderStates, OrderEvents> stateMachine = this.factory.getStateMachine(orderId);
        stateMachine.start();
        log.info("Current state: " + stateMachine.getState().getId().name());
        stateMachine.sendEvent(OrderEvents.PAY);
        log.info("Current state: " + stateMachine.getState().getId().name());
        stateMachine.sendEvent(MessageBuilder.withPayload(OrderEvents.FULFILL).build());
        log.info("Current state: " + stateMachine.getState().getId().name());

    }
}
