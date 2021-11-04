package io.artur.spring.orderservicesm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

/**
 *
 */
@Slf4j
@Configuration
@EnableStateMachineFactory
public class OrderStateMachineConfiguration extends StateMachineConfigurerAdapter<OrderStates, OrderEvents> {

    @Override
    public void configure(StateMachineTransitionConfigurer<OrderStates, OrderEvents> transitions) throws Exception {
        transitions.withExternal()
                .source(OrderStates.SUBMITTED).target(OrderStates.PAID).event(OrderEvents.PAY)
            .and().withExternal()
                .source(OrderStates.PAID).target(OrderStates.FULFILLED).event(OrderEvents.FULFILL)
            .and().withExternal()
                .source(OrderStates.SUBMITTED).target(OrderStates.CANCELED).event(OrderEvents.CANCEL)
            .and().withExternal()
                .source(OrderStates.PAID).target(OrderStates.CANCELED).event(OrderEvents.CANCEL);
    }

    @Override
    public void configure(StateMachineStateConfigurer<OrderStates, OrderEvents> states) throws Exception {
        states.withStates()
                .initial(OrderStates.SUBMITTED)
                .state(OrderStates.PAID)
                .end(OrderStates.FULFILLED)
                .end(OrderStates.CANCELED);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<OrderStates, OrderEvents> config) throws Exception {
        StateMachineListenerAdapter<OrderStates, OrderEvents> smListener =
                new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<OrderStates, OrderEvents> from, State<OrderStates, OrderEvents> to) {
                log.info("State changed from " + from + " to " + to);
            }
        };

        config.withConfiguration()
                .autoStartup(false)
                .listener(smListener);
    }
}
