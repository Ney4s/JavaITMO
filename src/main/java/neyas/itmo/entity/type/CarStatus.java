package neyas.itmo.entity.type;

import lombok.Getter;

import java.util.Map;
import java.util.Set;

public enum CarStatus {
    ORDERED("Заказан"),
    IN_STOCK("В наличии"),
    ON_TEST_DRIVE("На тест драйве"),
    SOLD("Продан"),
    RESERVED("Зарезервирована"),
    DELIVERING("Доставляется"),
    CANCELED("Отменен");

    @Getter private final String displayName;


    CarStatus(String displayName) {
        this.displayName = displayName;
    }

    private static final Map<CarStatus, Set<CarStatus>> TRANSITIONS = Map.of(
            ORDERED, Set.of(DELIVERING, CANCELED),
            DELIVERING, Set.of(IN_STOCK, RESERVED, CANCELED),
            IN_STOCK, Set.of(ON_TEST_DRIVE, RESERVED, SOLD),
            ON_TEST_DRIVE, Set.of(IN_STOCK),
            RESERVED, Set.of(SOLD, IN_STOCK),
            SOLD, Set.of(),
            CANCELED, Set.of()
    );

    private static final Set<CarStatus> WITH_VIN = Set.of(IN_STOCK, ON_TEST_DRIVE, RESERVED, SOLD);

    public Set<CarStatus> nextStatuses() {
        return TRANSITIONS.get(this);
    }

    public boolean canChangeTo(CarStatus newStatus) {
        return nextStatuses().contains(newStatus);
    }

    public boolean requiresVin() {
        return WITH_VIN.contains(this);
    }

    public boolean isTerminal() {
        return nextStatuses().isEmpty();
    }
}
