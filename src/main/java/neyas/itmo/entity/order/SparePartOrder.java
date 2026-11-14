package neyas.itmo.entity.order;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import neyas.itmo.entity.product.SparePart;
import neyas.itmo.entity.product.SparePartId;
import neyas.itmo.entity.type.OrderType;
import neyas.itmo.entity.user.Client;
import neyas.itmo.framework.NotNull;
import neyas.itmo.framework.PositiveOrZero;

import java.util.Map;
import java.util.Set;
import java.util.UUID;


@Entity
@Getter
@NoArgsConstructor
@DiscriminatorValue("SPARE_PART_ORDER")
public class SparePartOrder extends BasicOrder {

    @JoinColumn(name = "spare_part_id")
    @ManyToOne
    private SparePart sparePart;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "price")
    private long price;


    public SparePartOrder(Client client,
                          @NotNull SparePart sparePart,
                          @PositiveOrZero int quantity,
                          @PositiveOrZero long price
                          ) {
        super(client);
        this.sparePart = sparePart;
        this.quantity = quantity;
        this.price = price;
    }

    @Override
    public OrderType getOrderType() {
        return OrderType.SPARE_PART_ORDER;
    }

    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = Map.of(
            OrderStatus.CREATED, Set.of(OrderStatus.AWAITING_PAYMENT, OrderStatus.CANCELED),
            OrderStatus.AWAITING_PAYMENT, Set.of(OrderStatus.PAID, OrderStatus.CANCELED),
            OrderStatus.PAID, Set.of(OrderStatus.READY_FOR_PICKUP, OrderStatus.CANCELED),
            OrderStatus.READY_FOR_PICKUP, Set.of(OrderStatus.COMPLETED, OrderStatus.CANCELED),
            OrderStatus.COMPLETED, Set.of(),
            OrderStatus.CANCELED, Set.of()
    );

    @Override
    protected Map<OrderStatus, Set<OrderStatus>> allowedTransitions() {
        return TRANSITIONS;
    }
}
