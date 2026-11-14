package neyas.itmo.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import neyas.itmo.Validation;
import neyas.itmo.entity.configuration.Specification;
import neyas.itmo.entity.model.Model;
import neyas.itmo.entity.model.ModelId;
import neyas.itmo.entity.order.*;
import neyas.itmo.entity.product.Car;
import neyas.itmo.entity.product.SparePart;
import neyas.itmo.entity.product.SparePartId;
import neyas.itmo.entity.type.CarColor;
import neyas.itmo.entity.type.CarStatus;
import neyas.itmo.entity.type.OrderType;
import neyas.itmo.entity.user.Client;
import neyas.itmo.entity.user.Manager;
import neyas.itmo.exception.EntityNotFoundException;
import neyas.itmo.repository.CarRepository;
import neyas.itmo.repository.ModelRepository;
import neyas.itmo.repository.OrderRepository;
import neyas.itmo.repository.SparePartRepository;
import neyas.itmo.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    @NonNull private final OrderRepository orderRepository;
    @NonNull private final UserRepository userRepository;
    @NonNull private final CarRepository carRepository;
    @NonNull private final ModelRepository modelRepository;
    @NonNull private final SparePartRepository sparePartRepository;
    @NonNull private final CurrectUserService currectUserService;
    private final Random random = new Random();


    @PreAuthorize("hasRole('USER')")
    @Transactional
    public CarPurchasingOrder createPurchaseOrder(UUID carId){
        Client client = (Client) currectUserService.currectUser();
        Car car = requireCar(carId);
        Validation.require(car.isAvailable(), "Автомобиль недоступен для заказа");
        CarPurchasingOrder order = new CarPurchasingOrder(
                client, assignManager(), car, car.price());
        car.changeStatus(CarStatus.RESERVED);
        orderRepository.save(order);
        return order;
    }

    @PreAuthorize("hasRole('USER')")
    @Transactional
    public CustomCarOrder createCustomOrder(ModelId modelId, Specification specification, CarColor color){
        Client client = (Client) currectUserService.currectUser();
        Car car = carRepository.save(Car.ordered(requireModel(modelId), specification, color));
        CustomCarOrder order = new CustomCarOrder(
                client, assignManager(), car, specification.price());
        orderRepository.save(order);
        return order;
    }

    @PreAuthorize("hasRole('USER')")
    @Transactional
    public SparePartOrder createSparePartOrder(SparePartId sparePartId, int quantity){
        Client client = (Client) currectUserService.currectUser();
        SparePart sparePart = requireSparePart(sparePartId);
        Validation.require(sparePart.isInStock(quantity), "В наличии нет такого количества зап. частей");
        SparePartOrder order = new SparePartOrder(
                client, sparePart, quantity, sparePart.price() * quantity
        );
        sparePart.takeFromStock(quantity);
        orderRepository.save(order);
        return order;
    }

    @PreAuthorize("hasAnyRole('MANAGER','WAREHOUSE_ADMIN')")
    @Transactional
    public BasicOrder updateStatus(UUID orderId, OrderStatus newStatus){
        return applyStatus(orderId, newStatus);
    }

    @PreAuthorize("hasRole('MANAGER') or @orderSecurity.isOwner(#orderId)")
    @Transactional
    public BasicOrder cancel(@NonNull UUID orderId){
        return applyStatus(orderId, OrderStatus.CANCELED);
    }

    @PreAuthorize("hasAnyRole('MANAGER','WAREHOUSE_ADMIN') or @orderSecurity.isOwner(#orderId)")
    @Transactional(readOnly = true)
    public BasicOrder findById(@NonNull UUID orderId){
        return requireOrder(orderId);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @Transactional(readOnly = true)
    public List<BasicOrder> findAllByType(OrderType orderType){
        return orderRepository.findAll().stream()
                .filter(order -> order.getOrderType() == orderType)
                .toList();
    }

    @PreAuthorize("hasRole('MANAGER')")
    @Transactional(readOnly = true)
    public List<CarPurchasingOrder> findAllPurchasingOrders(){
        return orderRepository.findAllbyType(CarPurchasingOrder.class);
    }
    @PreAuthorize("hasRole('MANAGER')")
    @Transactional(readOnly = true)
    public List<CustomCarOrder> findAllCustomCarOrders(){
        return orderRepository.findAllbyType(CustomCarOrder.class);
    }
    @PreAuthorize("hasRole('MANAGER')")
    @Transactional(readOnly = true)
    public List<SparePartOrder> findAllSparePartOrders(){
        return orderRepository.findAllbyType(SparePartOrder.class);
    }
    @PreAuthorize("hasRole('MANAGER')")
    @Transactional(readOnly = true)
    public List<TestDriveOrder> findAllTestDriveOrders(){
        return orderRepository.findAllbyType(TestDriveOrder.class);
    }

    @PreAuthorize("hasRole('USER')")
    @Transactional(readOnly = true)
    public List<BasicOrder> findMyOrders(){
        Client client = (Client) currectUserService.currectUser();
        return orderRepository.findAllByClient(client);
    }

    private BasicOrder applyStatus(UUID orderId, OrderStatus newStatus){
        BasicOrder order = requireOrder(orderId);
        order.changeStatus(newStatus);
        synchronizeCar(order);
        return order;
    }

    private void synchronizeCar(BasicOrder order){
        if(order instanceof CarPurchasingOrder purchasingOrder){
            switch (purchasingOrder.getOrderStatus()){
                case COMPLETED -> applyStatus(purchasingOrder.getCar(), CarStatus.SOLD);
                case CANCELED -> applyStatus(purchasingOrder.getCar(), CarStatus.IN_STOCK);
                default -> {}
            }
        }
        if(order instanceof CustomCarOrder customCarOrder){
            switch (customCarOrder.getOrderStatus()){
                case AWAITING_DELIVERY ->  applyStatus(customCarOrder.getCar(), CarStatus.DELIVERING);
                case READY_FOR_PICKUP -> applyStatus(customCarOrder.getCar(), CarStatus.RESERVED);
                case COMPLETED -> applyStatus(customCarOrder.getCar(), CarStatus.SOLD);
                default -> {}
            }
        }
    }

    private void applyStatus(Car car, CarStatus newStatus){
        car.changeStatus(newStatus);
    }

    private Manager assignManager(){
        List<Manager> managers = userRepository.findAllbyType(Manager.class);
        return managers.get(random.nextInt(managers.size()));
    }

    Car requireCar(UUID carId){
        return carRepository.findById(carId)
                .orElseThrow(
                        () -> EntityNotFoundException.of(Car.class, carId));
    }

    Model requireModel(ModelId modelId){
        return modelRepository.findByCode(modelId)
                .orElseThrow(
                        () -> EntityNotFoundException.of(Model.class, modelId));
    }

    SparePart requireSparePart(SparePartId code){
        return sparePartRepository.findByCode(code)
                .orElseThrow(
                        () -> EntityNotFoundException.of(SparePart.class, code));
    }

    BasicOrder requireOrder(UUID orderId){
        return orderRepository.findById(orderId)
                .orElseThrow(
                        () -> EntityNotFoundException.of(BasicOrder.class, orderId));
    }




}
