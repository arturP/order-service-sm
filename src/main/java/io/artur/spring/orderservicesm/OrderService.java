package io.artur.spring.orderservicesm;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.access.StateMachineFunction;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

/**
 *
 */
@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final StateMachineFactory<OrderStates, OrderEvents> smFactory;
    private final static String ORDER_ID_HEADER = "orderId";
    private final static String CONFIRMATION_HEADER = "confirmation";
    private final static String FULFILLMENT_HEADER = "fulfillment";

    public Order create(Date date) {
        return orderRepository.save(
                Order.builder()
                .date(date)
                .state(OrderStates.SUBMITTED.name())
                .build());
    }

    public StateMachine<OrderStates, OrderEvents> pay(Long orderId, String confirmation) {
        StateMachine<OrderStates, OrderEvents> stateMachine = build(orderId);

        Message<OrderEvents> paymentMessage = MessageBuilder.withPayload(OrderEvents.PAY)
                .setHeader(ORDER_ID_HEADER, orderId)
                .setHeader(CONFIRMATION_HEADER, confirmation)
                .build();

        stateMachine.sendEvent(paymentMessage);

        return stateMachine;
    }

    public StateMachine<OrderStates, OrderEvents> fulfill(Long orderId, String fulfillmentInfo) {
        StateMachine<OrderStates, OrderEvents> stateMachine = build(orderId);

        Message<OrderEvents> fulfillmentMessage = MessageBuilder.withPayload(OrderEvents.FULFILL)
                .setHeader(ORDER_ID_HEADER, orderId)
                .setHeader(FULFILLMENT_HEADER, fulfillmentInfo)
                .build();

        stateMachine.sendEvent(fulfillmentMessage);

        return stateMachine;
    }

    public Order getOrder(Long id) {
    return orderRepository.findById(id).orElse(Order.builder().build());
    }

    private StateMachine<OrderStates, OrderEvents> build(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(Order.builder().state("").build());
        String state = order.getState();
        String orderIdKey = Long.toString(order.getId());
        StateMachine<OrderStates, OrderEvents> sm = smFactory.getStateMachine(orderIdKey);
        sm.stop();

        sm.getStateMachineAccessor().doWithAllRegions(
                new StateMachineFunction<StateMachineAccess<OrderStates, OrderEvents>>() {
                    @Override
                    public void apply(StateMachineAccess<OrderStates, OrderEvents> function) {
                        function.addStateMachineInterceptor(getInterceptor());

                        OrderStates current = OrderStates.valueOf(state);
                        function.resetStateMachine(
                                new DefaultStateMachineContext<>(current, null, null, null));
                    }
                }
        );

        sm.start();

        return sm;
    }

    private StateMachineInterceptorAdapter<OrderStates, OrderEvents> getInterceptor() {
    return new StateMachineInterceptorAdapter<>() {
      @Override
      public void preStateChange(
          State<OrderStates, OrderEvents> state,
          Message<OrderEvents> message,
          Transition<OrderStates, OrderEvents> transition,
          StateMachine<OrderStates, OrderEvents> stateMachine) {
        Optional.ofNullable(message)
            .ifPresent(
                msg -> {
                  Optional.ofNullable(
                          Long.class.cast(msg.getHeaders().getOrDefault(ORDER_ID_HEADER, -1L)))
                      .ifPresent(
                          orderId -> {
                            Order order =
                                orderRepository.findById(orderId).orElse(Order.builder().build());
                            order.setState(state.getId().name());
                            orderRepository.save(order);
                          });
                });
      }
    };
    }
}
