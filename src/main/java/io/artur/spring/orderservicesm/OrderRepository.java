package io.artur.spring.orderservicesm;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 */
public interface OrderRepository extends JpaRepository<Order, Long> {
}
