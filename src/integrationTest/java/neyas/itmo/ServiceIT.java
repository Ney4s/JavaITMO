package neyas.itmo;

import neyas.itmo.entity.CarFilter;
import neyas.itmo.entity.ModelFilter;
import neyas.itmo.entity.configuration.ComponentOptionId;
import neyas.itmo.entity.configuration.Specification;
import neyas.itmo.entity.model.Model;
import neyas.itmo.entity.model.ModelId;
import neyas.itmo.entity.order.BasicOrder;
import neyas.itmo.entity.order.CarPurchasingOrder;
import neyas.itmo.entity.order.OrderStatus;
import neyas.itmo.entity.order.TestDriveOrder;
import neyas.itmo.entity.product.Car;
import neyas.itmo.entity.product.SparePart;
import neyas.itmo.entity.product.SparePartId;
import neyas.itmo.entity.product.Vin;
import neyas.itmo.entity.type.CarColor;
import neyas.itmo.entity.type.CarStatus;
import neyas.itmo.entity.type.ComponentType;
import neyas.itmo.entity.type.FuelType;
import neyas.itmo.entity.type.OrderType;
import neyas.itmo.entity.type.UserRole;
import neyas.itmo.entity.user.User;
import neyas.itmo.exception.DomainValidationException;
import neyas.itmo.exception.EntityNotFoundException;
import neyas.itmo.exception.IncompatibleComponentException;
import neyas.itmo.repository.CarRepository;
import neyas.itmo.repository.ModelRepository;
import neyas.itmo.repository.UserRepository;
import neyas.itmo.service.CarCatalogService;
import neyas.itmo.service.CarConfigurationService;
import neyas.itmo.service.OrderService;
import neyas.itmo.service.SparePartService;
import neyas.itmo.service.TestDriveService;
import neyas.itmo.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class ServiceIT extends IntegrationTestBase {

    private static final UUID CAR_IN_STOCK = UUID.fromString("44444444-4444-4444-4444-444444444401");
    private static final UUID TEST_DRIVE_CAR = UUID.fromString("44444444-4444-4444-4444-444444444402");

    @Autowired
    private CarCatalogService carCatalogService;
    @Autowired
    private CarConfigurationService carConfigurationService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private SparePartService sparePartService;
    @Autowired
    private TestDriveService testDriveService;
    @Autowired
    private UserService userService;
    @Autowired
    private CarRepository carRepository;
    @Autowired
    private ModelRepository modelRepository;
    @Autowired
    private UserRepository userRepository;

    private User client;
    private User manager;
    private User warehouseAdmin;
    private User admin;

    @BeforeEach
    void loadUsers() {
        client = userRepository.findByEmail("client@autosalon.ru").orElseThrow();
        manager = userRepository.findByEmail("manager@autosalon.ru").orElseThrow();
        warehouseAdmin = userRepository.findByEmail("warehouse@autosalon.ru").orElseThrow();
        admin = userRepository.findByEmail("admin@autosalon.ru").orElseThrow();
    }

    @AfterEach
    void clearSession() {
        logout();
    }

    @Test
    void showsOnlyAvailableCars() {
        loginAs(client);

        assertEquals(2, carCatalogService.findAvailableCars(
                CarFilter.builder().carStatus(CarStatus.IN_STOCK).build()).size());
        assertEquals(1, carCatalogService.getTestDriveCars().size());
    }

    @Test
    void appliesFiltersToAvailableCars() {
        loginAs(client);

        assertEquals(2, carCatalogService
                .findAvailableCars(CarFilter.builder().manufacturer("BMW").model("320i").build()).size());
        assertEquals(0, carCatalogService
                .findAvailableCars(CarFilter.builder().manufacturer("Lada").build()).size());
        assertEquals(0, carCatalogService
                .findAvailableCars(CarFilter.builder().fuelType(FuelType.DIESEL).build()).size());
        assertEquals(0, carCatalogService
                .findAvailableCars(CarFilter.builder().maxPrice(1_000_000L).build()).size());
    }

    @Test
    void findsModelsByFilter() {
        loginAs(client);

        assertEquals(2, carCatalogService.findModels(CarFilter.empty()).size());
        assertEquals(1, carCatalogService
                .findModels(CarFilter.builder().manufacturer("BMW").model("330i").build()).size());
        assertEquals(1, carCatalogService.findModels(ModelFilter.builder()
                .components(Set.of(new ComponentOptionId("INT-PERFORMANCE")))
                .build()).size());
    }

    @Test
    void readsSingleCar() {
        loginAs(client);

        assertEquals(new Vin("WBA8E9G50GNT12345"), carCatalogService.getCar(CAR_IN_STOCK).getVin());
        assertThrows(EntityNotFoundException.class,
                () -> carCatalogService.getCar(UUID.randomUUID()));
    }

    @Test
    void warehouseAddsCarOfKnownModel() {
        loginAs(warehouseAdmin);
        Model model = modelRepository.findByCode(new ModelId("BMW-320I")).orElseThrow();
        Car car = new Car(model, Specification.base(model),
                new Vin("WBA8E9C50KG111222"), CarColor.GREEN, CarStatus.IN_STOCK);

        assertEquals(car, carCatalogService.addCar(car));
        assertEquals(4, carRepository.count());
    }

    @Test
    void rejectsDuplicateVin() {
        loginAs(warehouseAdmin);
        Model model = modelRepository.findByCode(new ModelId("BMW-320I")).orElseThrow();
        Car car = new Car(model, Specification.base(model),
                new Vin("WBA8E9G50GNT12345"), CarColor.GREEN, CarStatus.IN_STOCK);

        assertThrows(DomainValidationException.class, () -> carCatalogService.addCar(car));
    }

    @Test
    void managesTestDriveList() {
        loginAs(admin);

        carCatalogService.addToTestDriveCars(CAR_IN_STOCK);
        assertEquals(2, carCatalogService.getTestDriveCars().size());

        carCatalogService.removeFromTestDriveCars(CAR_IN_STOCK);
        assertEquals(1, carCatalogService.getTestDriveCars().size());
    }

    @Test
    void assignsVinToOrderedCar() {
        loginAs(warehouseAdmin);
        Model model = modelRepository.findByCode(new ModelId("BMW-320I")).orElseThrow();
        Car ordered = carRepository.save(Car.ordered(model, Specification.base(model), CarColor.RED));
        ordered.changeStatus(CarStatus.DELIVERING);

        Vin vin = new Vin("WBA8E9C50KG111222");
        assertEquals(vin, carCatalogService.assignVin(ordered.getId(), vin).getVin());
    }

    @Test
    void rejectsForbiddenStatusChange() {
        loginAs(warehouseAdmin);

        assertThrows(DomainValidationException.class,
                () -> carCatalogService.changeCarStatus(CAR_IN_STOCK, CarStatus.DELIVERING));
    }

    @Test
    void removesOnlyCanceledCar() {
        loginAs(warehouseAdmin);
        Model model = modelRepository.findByCode(new ModelId("BMW-320I")).orElseThrow();
        Car ordered = carRepository.save(Car.ordered(model, Specification.base(model), CarColor.RED));
        ordered.changeStatus(CarStatus.CANCELED);

        assertThrows(DomainValidationException.class, () -> carCatalogService.removeCar(CAR_IN_STOCK));
        carCatalogService.removeCar(ordered.getId());
        assertTrue(carRepository.findById(ordered.getId()).orElseThrow().isRemoved());
    }

    @Test
    void configuratorCalculatesBaseAndCustomPrice() {
        loginAs(client);
        ModelId modelId = new ModelId("BMW-320I");

        assertEquals(3_380_000, carConfigurationService.baseSpecification(modelId).price());
        assertEquals(3_515_000, carConfigurationService.calculatePrice(modelId, customSelection()));
        assertEquals(ComponentType.values().length,
                carConfigurationService.availableOptions(modelId).size());
    }

    @Test
    void configuratorRejectsIncompatibleComponent() {
        loginAs(client);
        Map<ComponentType, ComponentOptionId> selection = customSelection();
        selection.put(ComponentType.INTERIOR, new ComponentOptionId("INT-PERFORMANCE"));

        assertThrows(IncompatibleComponentException.class,
                () -> carConfigurationService.configure(new ModelId("BMW-320I"), selection));
    }

    @Test
    void configuratorRejectsSelectionWithoutRequiredNode() {
        loginAs(client);
        Map<ComponentType, ComponentOptionId> selection = customSelection();
        selection.remove(ComponentType.INTERIOR);

        assertThrows(DomainValidationException.class,
                () -> carConfigurationService.configure(new ModelId("BMW-320I"), selection));
    }

    @Test
    void clientOrdersCarInStock() {
        loginAs(client);
        CarPurchasingOrder order = orderService.createPurchaseOrder(CAR_IN_STOCK);

        assertEquals(OrderStatus.CREATED, order.getOrderStatus());
        assertEquals(3_380_000, order.getPrice());
        assertNotNull(order.getManager());
        assertEquals(CarStatus.RESERVED, carRepository.findById(CAR_IN_STOCK).orElseThrow().carStatus());
    }

    @Test
    void rejectsOrderOfUnavailableCar() {
        loginAs(client);

        assertThrows(DomainValidationException.class,
                () -> orderService.createPurchaseOrder(TEST_DRIVE_CAR));
    }

    @Test
    void orderMovesCarThroughPipeline() {
        loginAs(client);
        CarPurchasingOrder order = orderService.createPurchaseOrder(CAR_IN_STOCK);

        loginAs(manager);
        orderService.updateStatus(order.getId(), OrderStatus.APPROVED_BY_MANAGER);
        orderService.updateStatus(order.getId(), OrderStatus.AWAITING_PAYMENT);
        orderService.updateStatus(order.getId(), OrderStatus.PAID);
        orderService.updateStatus(order.getId(), OrderStatus.READY_FOR_PICKUP);
        BasicOrder completed = orderService.updateStatus(order.getId(), OrderStatus.COMPLETED);

        assertTrue(completed.isCompleted());
        assertEquals(CarStatus.SOLD, carRepository.findById(CAR_IN_STOCK).orElseThrow().carStatus());
    }

    @Test
    void canceledOrderReturnsCarToStock() {
        loginAs(client);
        CarPurchasingOrder order = orderService.createPurchaseOrder(CAR_IN_STOCK);
        orderService.cancel(order.getId());

        assertEquals(CarStatus.IN_STOCK, carRepository.findById(CAR_IN_STOCK).orElseThrow().carStatus());
    }

    @Test
    void clientOrdersCarWithCustomConfiguration() {
        loginAs(client);
        ModelId modelId = new ModelId("BMW-320I");
        Specification specification = carConfigurationService.configure(modelId, customSelection());

        assertEquals(3_515_000,
                orderService.createCustomOrder(modelId, specification, CarColor.RED).getPrice());
    }

    @Test
    void clientOrdersSparePartAndStockDecreases() {
        loginAs(client);
        SparePartId code = new SparePartId("SP-OIL-FILTER");

        assertEquals(12_500, orderService.createSparePartOrder(code, 5).getPrice());
        assertEquals(115, sparePartService.getByCode(code).quantityInStock());
    }

    @Test
    void rejectsSparePartOrderOverStock() {
        loginAs(client);

        assertThrows(DomainValidationException.class,
                () -> orderService.createSparePartOrder(new SparePartId("SP-OIL-FILTER"), 500));
    }

    @Test
    void managerSeesOrdersOfEveryKind() {
        loginAs(client);
        orderService.createPurchaseOrder(CAR_IN_STOCK);

        loginAs(manager);
        assertEquals(1, orderService.findAllPurchasingOrders().size());
        assertEquals(1, orderService.findAllByType(OrderType.CAR_PURCHASING_ORDER).size());
        assertEquals(0, orderService.findAllCustomCarOrders().size());
        assertEquals(0, orderService.findAllSparePartOrders().size());
    }

    @Test
    void clientSeesOnlyOwnOrders() {
        loginAs(client);
        orderService.createPurchaseOrder(CAR_IN_STOCK);

        assertEquals(1, orderService.findMyOrders().size());
    }

    @Test
    void warehouseChangesSparePartQuantity() {
        loginAs(warehouseAdmin);
        SparePartId code = new SparePartId("SP-BRAKE-PADS");

        assertEquals(7, sparePartService.changeQuantity(code, 7).quantityInStock());
    }

    @Test
    void warehouseAddsSparePart() {
        loginAs(warehouseAdmin);
        Model model = modelRepository.findByCode(new ModelId("BMW-330I")).orElseThrow();
        SparePart part = new SparePart(new SparePartId("SP-AIR-FILTER"),
                "Воздушный фильтр", 3_000, 50, Set.of(model));

        sparePartService.addSparePart(part);
        assertEquals(2, sparePartService.findAllFittingTo(new ModelId("BMW-330I")).size());
    }

    @Test
    void rejectsDuplicateSparePart() {
        loginAs(warehouseAdmin);
        Model model = modelRepository.findByCode(new ModelId("BMW-320I")).orElseThrow();
        SparePart part = new SparePart(new SparePartId("SP-BRAKE-PADS"),
                "Тормозные колодки", 18_000, 5, Set.of(model));

        assertThrows(DomainValidationException.class, () -> sparePartService.addSparePart(part));
    }

    @Test
    void clientRequestsTestDrive() {
        loginAs(client);
        TestDriveOrder order = testDriveService.requestTestDrive(
                TEST_DRIVE_CAR, LocalDateTime.now().plusDays(1));

        assertEquals(OrderStatus.CREATED, order.getOrderStatus());

        loginAs(manager);
        assertEquals(1, testDriveService.findAll().size());
        assertEquals(1, testDriveService.activeOrders().size());
    }

    @Test
    void rejectsTestDriveOnCarThatIsNotOnTestDrive() {
        loginAs(client);

        assertThrows(DomainValidationException.class,
                () -> testDriveService.requestTestDrive(CAR_IN_STOCK, LocalDateTime.now().plusDays(1)));
    }

    @Test
    void rejectsTestDriveInThePast() {
        loginAs(client);

        assertThrows(DomainValidationException.class,
                () -> testDriveService.requestTestDrive(TEST_DRIVE_CAR, LocalDateTime.now().minusDays(1)));
    }

    @Test
    void rejectsOverlappingTestDrive() {
        loginAs(client);
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        testDriveService.requestTestDrive(TEST_DRIVE_CAR, start);

        assertThrows(DomainValidationException.class,
                () -> testDriveService.requestTestDrive(TEST_DRIVE_CAR, start.plusMinutes(30)));
    }

    @Test
    void registersClient() {
        loginAs(admin);
        userService.registerClient("Новый Клиент", "new@mail.ru", "+79990000009");

        assertEquals("new@mail.ru", userRepository.findByEmail("new@mail.ru").orElseThrow().getEmail());
    }

    @Test
    void rejectsDuplicateEmail() {
        loginAs(admin);

        assertThrows(DomainValidationException.class,
                () -> userService.registerClient("Дубль", "client@autosalon.ru", "+79990000008"));
    }

    @Test
    void rejectsInvalidContacts() {
        loginAs(admin);

        assertThrows(DomainValidationException.class,
                () -> userService.registerClient("Кривая почта", "not-a-mail", "+79990000007"));
        assertThrows(DomainValidationException.class,
                () -> userService.registerClient("Кривой номер", "ok@mail.ru", "8-900-000"));
    }

    @Test
    void adminListsUsersByRole() {
        loginAs(admin);

        assertEquals(4, userService.findAll().size());
        assertEquals(1, userService.findAllByRole(UserRole.MANAGER).size());
        assertEquals(1, userService.findAllByRole(UserRole.CLIENT).size());
    }

    @Test
    void adminDeletesUser() {
        loginAs(admin);
        userService.deleteUserById(client.getId());

        assertTrue(userRepository.findById(client.getId()).orElseThrow().isRemoved());
    }

    @Test
    void deniesOperationWithoutRequiredRole() {
        loginAs(client);

        assertThrows(AccessDeniedException.class,
                () -> carCatalogService.changeCarStatus(CAR_IN_STOCK, CarStatus.RESERVED));
        assertThrows(AccessDeniedException.class, () -> userService.findAll());
    }

    @Test
    void adminPassesEveryRoleCheck() {
        loginAs(admin);

        assertEquals(3, carCatalogService.findAll().size());
        assertEquals(1, carCatalogService.getTestDriveCars().size());
    }

    @Test
    void deniesOperationWithoutAuthenticatedUser() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> carCatalogService.findAll());
    }

    private Map<ComponentType, ComponentOptionId> customSelection() {
        Map<ComponentType, ComponentOptionId> selection = new LinkedHashMap<>();
        selection.put(ComponentType.ENGINE, new ComponentOptionId("ENG-320I"));
        selection.put(ComponentType.BODY, new ComponentOptionId("BODY-SEDAN"));
        selection.put(ComponentType.DRIVE, new ComponentOptionId("DRIVE-RWD"));
        selection.put(ComponentType.TRANSMISSION, new ComponentOptionId("TR-8AT"));
        selection.put(ComponentType.STEERING_WHEEL, new ComponentOptionId("SW-MSPORT"));
        selection.put(ComponentType.INTERIOR, new ComponentOptionId("INT-DAKOTA"));
        return selection;
    }
}
