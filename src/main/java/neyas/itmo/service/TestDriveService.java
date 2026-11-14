package neyas.itmo.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import neyas.itmo.Validation;
import neyas.itmo.entity.order.TestDriveOrder;
import neyas.itmo.entity.product.Car;
import neyas.itmo.entity.user.Client;
import neyas.itmo.exception.EntityNotFoundException;
import neyas.itmo.repository.CarRepository;
import neyas.itmo.repository.OrderRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TestDriveService {
    @NonNull private final OrderRepository orderRepository;
    @NonNull private final CarRepository carRepository;
    @NonNull private final CurrectUserService currectUserService;

    @PreAuthorize("hasRole('MANAGER')")
    @Transactional(readOnly = true)
    public List<TestDriveOrder> findAll() {
        return orderRepository.findAllbyType(TestDriveOrder.class);
    }

    @PreAuthorize("hasRole('USER')")
    @Transactional
    public TestDriveOrder requestTestDrive( @NonNull UUID carId,
                                            @NonNull LocalDateTime startsAt)
    {
        Client client = (Client) currectUserService.currectUser();
        Validation.require(startsAt.isAfter(LocalDateTime.now()),
                "Тест драйв нельзя заказать из будущего");

        Car car = requireCar(carId);
        Validation.require(car.isOnTestDrive(),
                "Автомобиль %s не может участвовать в тест-драйве".formatted(carId));

        TestDriveOrder order = new TestDriveOrder(client, car, startsAt);
        Validation.require(ordersForCar(carId)
                        .stream()
                        .noneMatch(existingOrder -> existingOrder.overlaps(order)),
                "На это время автомобиль уже забронирован");
        orderRepository.save(order);
        return order;
    }

    @PreAuthorize("hasRole('MANAGER')")
    @Transactional(readOnly = true)
    public List<TestDriveOrder> activeOrders() {
        return orderRepository.findAllbyType(TestDriveOrder.class).stream()
                .filter(order -> !order.getOrderStatus().isTerminal())
                .toList();
    }

    @PreAuthorize("hasRole('MANAGER')")
    @Transactional(readOnly = true)
    public List<TestDriveOrder> findAllForCar(@NonNull UUID carId) {
        return ordersForCar(carId);
    }

    List<TestDriveOrder> ordersForCar(UUID carId) {
        return orderRepository.findAllbyType(TestDriveOrder.class).stream()
                .filter(order -> order.getCar().getId().equals(carId))
                .toList();
    }



    Car requireCar(UUID carId) {
        return carRepository.findById(carId)
                .orElseThrow(
                        () -> EntityNotFoundException.of(Car.class, carId));
    }

}
