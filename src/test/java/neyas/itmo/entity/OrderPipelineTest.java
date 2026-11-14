package neyas.itmo.entity;

import neyas.itmo.Fixtures;
import neyas.itmo.entity.order.*;
import neyas.itmo.entity.product.Car;
import neyas.itmo.entity.type.OrderType;
import neyas.itmo.entity.user.Client;
import neyas.itmo.entity.user.Manager;
import neyas.itmo.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OrderPipelineTest {

    private final Client client = Fixtures.client();
    private final Manager manager = Fixtures.manager();
    private final Car car = Fixtures.carInStock();

    private CarPurchasingOrder purchasingOrder() {
        return new CarPurchasingOrder(client, manager, car, 3_380_000);
    }

    private CustomCarOrder customOrder() {
        return new CustomCarOrder(client, manager, car, 3_515_000);
    }

    private void walk(BasicOrder order, OrderStatus... statuses) {
        for (OrderStatus status : statuses) {
            order.changeStatus(status);
            assertEquals(status, order.getOrderStatus());
        }
    }

    @Test
    void newOrderIsCreatedAndLinkedToParticipants() {
        CarPurchasingOrder order = purchasingOrder();

        assertEquals(OrderStatus.CREATED, order.getOrderStatus());
        assertEquals(client, order.getClient());
        assertEquals(manager, order.getManager());
        assertEquals(car, order.getCar());
        assertEquals(3_380_000, order.getPrice());
        assertEquals(OrderType.CAR_PURCHASING_ORDER, order.getOrderType());
        assertFalse(order.isCompleted());
    }

    @Test
    void purchasingOrderGoesThroughManagerApproval() {
        CarPurchasingOrder order = purchasingOrder();

        walk(order, OrderStatus.APPROVED_BY_MANAGER, OrderStatus.AWAITING_PAYMENT,
                OrderStatus.PAID, OrderStatus.READY_FOR_PICKUP, OrderStatus.COMPLETED);

        assertTrue(order.isCompleted());
        assertTrue(order.nextStatuses().isEmpty());
    }

    @Test
    void customOrderGoesThroughWarehouseAndDelivery() {
        CustomCarOrder order = customOrder();

        walk(order, OrderStatus.APPROVED_BY_WAREHOUSE, OrderStatus.AWAITING_PAYMENT, OrderStatus.PAID,
                OrderStatus.AWAITING_DELIVERY, OrderStatus.READY_FOR_PICKUP, OrderStatus.COMPLETED);

        assertTrue(order.isCompleted());
    }

    @Test
    void purchasingOrderIsNotApprovedByWarehouse() {
        CarPurchasingOrder order = purchasingOrder();

        assertThrows(DomainValidationException.class,
                () -> order.changeStatus(OrderStatus.APPROVED_BY_WAREHOUSE));
        assertEquals(OrderStatus.CREATED, order.getOrderStatus());
    }

    @Test
    void customOrderCannotSkipDelivery() {
        CustomCarOrder order = customOrder();
        walk(order, OrderStatus.APPROVED_BY_WAREHOUSE, OrderStatus.AWAITING_PAYMENT, OrderStatus.PAID);

        assertThrows(DomainValidationException.class,
                () -> order.changeStatus(OrderStatus.READY_FOR_PICKUP));
    }

    @Test
    void statusCannotBeSkipped() {
        DomainValidationException exception = assertThrows(DomainValidationException.class,
                () -> purchasingOrder().changeStatus(OrderStatus.PAID));
        assertTrue(exception.getMessage().contains("Недопустимый переход"));
    }

    @Test
    void orderIsCanceledOnAnyIntermediateStep() {
        CarPurchasingOrder order = purchasingOrder();
        order.changeStatus(OrderStatus.APPROVED_BY_MANAGER);
        order.cancel();

        assertTrue(order.isCanceled());
        assertTrue(OrderStatus.CANCELED.isTerminal());
        assertFalse(OrderStatus.PAID.isTerminal());
    }

    @Test
    void completedOrderCannotBeCanceled() {
        CarPurchasingOrder order = purchasingOrder();
        walk(order, OrderStatus.APPROVED_BY_MANAGER, OrderStatus.AWAITING_PAYMENT,
                OrderStatus.PAID, OrderStatus.READY_FOR_PICKUP, OrderStatus.COMPLETED);

        assertThrows(DomainValidationException.class, order::cancel);
    }

    @Test
    void sparePartOrderGoesStraightToPayment() {
        SparePartOrder order = new SparePartOrder(client, Fixtures.brakePads(), 2, 24_000);

        walk(order, OrderStatus.AWAITING_PAYMENT, OrderStatus.PAID,
                OrderStatus.READY_FOR_PICKUP, OrderStatus.COMPLETED);
        assertEquals(OrderType.SPARE_PART_ORDER, order.getOrderType());
        assertEquals(2, order.getQuantity());
    }

    @Test
    void testDriveOrderIsApprovedByManager() {
        TestDriveOrder order = new TestDriveOrder(client, car, LocalDateTime.now().plusDays(1));

        walk(order, OrderStatus.APPROVED_BY_MANAGER, OrderStatus.COMPLETED);
        assertEquals(OrderType.TEST_DRIVE, order.getOrderType());
    }

    @Test
    void testDriveOrdersOverlapOnSameCar() {
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 12, 0);
        TestDriveOrder first = new TestDriveOrder(client, car, start);
        TestDriveOrder overlapping = new TestDriveOrder(client, car, start.plusMinutes(30));
        TestDriveOrder later = new TestDriveOrder(client, car, start.plusHours(2));
        TestDriveOrder otherCar = new TestDriveOrder(client, Fixtures.carInStock(Fixtures.VIN_WHITE), start);

        assertEquals(start.plusHours(1), first.endsAt());
        assertTrue(first.overlaps(overlapping));
        assertFalse(first.overlaps(later));
        assertFalse(first.overlaps(otherCar));
    }

    @Test
    void testDriveLastsOneHour() {
        TestDriveOrder order = new TestDriveOrder(client, car, LocalDateTime.of(2030, 1, 1, 12, 0));

        assertEquals(LocalDateTime.of(2030, 1, 1, 13, 0), order.endsAt());
    }
}
