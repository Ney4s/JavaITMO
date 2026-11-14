package neyas.itmo.entity.order;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import neyas.itmo.entity.product.Car;
import neyas.itmo.entity.type.OrderType;
import neyas.itmo.entity.user.Client;
import neyas.itmo.entity.user.Manager;
import neyas.itmo.framework.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@DiscriminatorValue("TEST_DRIVE")
public class TestDriveOrder extends BasicOrder
{
    public static final Duration DURATION = Duration.ofHours(1);

    @ManyToOne
    @JoinColumn(name = "car_id")
    private Car car;

    @Column(name = "starts_at")
    private LocalDateTime startsAt;


    public TestDriveOrder(Client client,
                          @NotNull Car car,
                          @NotNull LocalDateTime startsAt) {
        super(client);
        this.car = car;
        this.startsAt = startsAt;
    }

    @Override
    public OrderType getOrderType()
    {
        return OrderType.TEST_DRIVE;
    }

    public boolean overlaps(TestDriveOrder other) {
        return car.equals(other.car)
                && startsAt.isBefore(other.endsAt())
                && other.startsAt.isBefore(endsAt());
    }

    public LocalDateTime endsAt() {
        return startsAt.plus(DURATION);
    }

    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = Map.of(
            OrderStatus.CREATED, Set.of(OrderStatus.APPROVED_BY_MANAGER, OrderStatus.CANCELED),
            OrderStatus.APPROVED_BY_MANAGER, Set.of(OrderStatus.COMPLETED, OrderStatus.CANCELED),
            OrderStatus.COMPLETED, Set.of(),
            OrderStatus.CANCELED, Set.of()
    );

    @Override
    protected Map<OrderStatus, Set<OrderStatus>> allowedTransitions() {
        return TRANSITIONS;
    }
}
