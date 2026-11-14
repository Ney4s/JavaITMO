package neyas.itmo.entity.order;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;
import neyas.itmo.entity.product.Car;
import neyas.itmo.entity.type.OrderType;
import neyas.itmo.entity.user.Client;
import neyas.itmo.entity.user.Manager;
import neyas.itmo.framework.NotBlank;
import neyas.itmo.framework.NotNull;
import neyas.itmo.framework.PositiveOrZero;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor
@DiscriminatorValue("CUSTOM_CAR_ORDER")
public class CustomCarOrder extends ManagedOrder
{

    public CustomCarOrder(Client client,
                          @NotNull Manager manager,
                          @NotBlank Car car,
                          @PositiveOrZero long price) {
        super(client,car,manager,price);

    }
    @Override
    public OrderType getOrderType() {
        return OrderType.CUSTOM_CAR_ORDER;
    }

    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = Map.of(
            OrderStatus.CREATED, Set.of(OrderStatus.APPROVED_BY_WAREHOUSE, OrderStatus.CANCELED),
            OrderStatus.APPROVED_BY_WAREHOUSE, Set.of(OrderStatus.AWAITING_PAYMENT, OrderStatus.CANCELED),
            OrderStatus.AWAITING_PAYMENT, Set.of(OrderStatus.PAID, OrderStatus.CANCELED),
            OrderStatus.PAID, Set.of(OrderStatus.AWAITING_DELIVERY, OrderStatus.CANCELED),
            OrderStatus.AWAITING_DELIVERY, Set.of(OrderStatus.READY_FOR_PICKUP, OrderStatus.CANCELED),
            OrderStatus.READY_FOR_PICKUP, Set.of(OrderStatus.COMPLETED, OrderStatus.CANCELED),
            OrderStatus.COMPLETED, Set.of(),
            OrderStatus.CANCELED, Set.of()
    );

    @Override
    protected Map<OrderStatus, Set<OrderStatus>> allowedTransitions() {
        return TRANSITIONS;
    }
}
