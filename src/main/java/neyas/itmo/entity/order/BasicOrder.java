package neyas.itmo.entity.order;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import neyas.itmo.Validation;
import neyas.itmo.entity.BaseEntity;
import neyas.itmo.entity.type.OrderType;
import neyas.itmo.entity.user.Client;
import neyas.itmo.framework.NotNull;

import java.util.Map;
import java.util.Set;


@Entity
@Table(name = "orders")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "order_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@NoArgsConstructor
public abstract class BasicOrder extends BaseEntity {


    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus orderStatus = OrderStatus.CREATED;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    protected BasicOrder(Client client) {
        this.client = client;
    }


    public void changeStatus(@NotNull OrderStatus orderStatus){
        Validation.require(nextStatuses().contains(orderStatus),
                "Недопустимый переход статуса заказа: %s -> %s".formatted(this.orderStatus, orderStatus));

        this.orderStatus = orderStatus;
    }

    public abstract OrderType getOrderType();


    protected abstract Map<OrderStatus, Set<OrderStatus>> allowedTransitions();

    public Set<OrderStatus> nextStatuses(){
        return allowedTransitions().getOrDefault(orderStatus, Set.of());
    }

    public void cancel(){
        changeStatus(OrderStatus.CANCELED);
    }

    public boolean isCompleted(){
        return orderStatus == OrderStatus.COMPLETED;
    }

    public boolean isCanceled(){
        return orderStatus == OrderStatus.CANCELED;
    }




}
