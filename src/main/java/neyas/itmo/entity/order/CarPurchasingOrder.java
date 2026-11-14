package neyas.itmo.entity.order;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;
import neyas.itmo.entity.product.Car;
import neyas.itmo.entity.type.OrderType;
import neyas.itmo.entity.user.Client;
import neyas.itmo.entity.user.Manager;
import neyas.itmo.framework.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor
@DiscriminatorValue("CAR_PURCHASE_ORDER")
public class CarPurchasingOrder extends ManagedOrder {


    public CarPurchasingOrder(Client client,
                              Manager manager,
                              Car car,
                              long price
    ) {
        super(client,car,manager,price);
    }

    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = Map.of(
            OrderStatus.CREATED, Set.of(OrderStatus.APPROVED_BY_MANAGER, OrderStatus.CANCELED),
            OrderStatus.APPROVED_BY_MANAGER, Set.of(OrderStatus.AWAITING_PAYMENT, OrderStatus.CANCELED),
            OrderStatus.AWAITING_PAYMENT, Set.of(OrderStatus.PAID, OrderStatus.CANCELED),
            OrderStatus.PAID, Set.of(OrderStatus.READY_FOR_PICKUP, OrderStatus.CANCELED),
            OrderStatus.READY_FOR_PICKUP, Set.of(OrderStatus.COMPLETED, OrderStatus.CANCELED),
            OrderStatus.COMPLETED, Set.of(),
            OrderStatus.CANCELED, Set.of()
    );

    @Override
    public OrderType getOrderType() {
        return OrderType.CAR_PURCHASING_ORDER;
    }

    @Override
    protected Map<OrderStatus, Set<OrderStatus>> allowedTransitions() {
        return TRANSITIONS;
    }

}
