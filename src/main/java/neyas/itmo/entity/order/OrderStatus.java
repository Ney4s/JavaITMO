package neyas.itmo.entity.order;

import java.util.Set;

public enum OrderStatus {
    CREATED,
    APPROVED_BY_MANAGER,
    APPROVED_BY_WAREHOUSE,
    AWAITING_PAYMENT,
    PAID,
    AWAITING_DELIVERY,
    READY_FOR_PICKUP,
    CANCELED,
    COMPLETED;

    private static final Set<OrderStatus> TERMINAL = Set.of(COMPLETED, CANCELED);

    public boolean isTerminal() {
        return TERMINAL.contains(this);
    }
}
