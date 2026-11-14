package neyas.itmo.entity.order;

import jakarta.persistence.Column;
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
import neyas.itmo.framework.PositiveOrZero;

import java.util.UUID;


@Entity
@Getter
@NoArgsConstructor
public abstract class ManagedOrder extends BasicOrder{
    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Manager manager;

    @ManyToOne
    @JoinColumn(name = "car_id")
    private Car car;

    @Column(name = "price")
    private long price;

    protected  ManagedOrder(Client client,
                            @NotNull Car car,
                            @NotNull Manager manager,
                            @PositiveOrZero long price)
    {
        super(client);
        this.car = car;
        this.manager = manager;
        this.price = price;
    }





}
