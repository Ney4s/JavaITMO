package neyas.itmo;

import neyas.itmo.entity.CarFilter;
import neyas.itmo.entity.ModelFilter;
import neyas.itmo.entity.configuration.ComponentOptionId;
import neyas.itmo.entity.model.Model;
import neyas.itmo.entity.model.ModelId;
import neyas.itmo.entity.product.Car;
import neyas.itmo.entity.product.SparePart;
import neyas.itmo.entity.product.SparePartId;
import neyas.itmo.entity.product.Vin;
import neyas.itmo.entity.type.BodyType;
import neyas.itmo.entity.type.CarStatus;
import neyas.itmo.entity.type.ComponentType;
import neyas.itmo.entity.type.DriveType;
import neyas.itmo.entity.type.FuelType;
import neyas.itmo.entity.type.TransmissionType;
import neyas.itmo.entity.user.Manager;
import neyas.itmo.entity.user.User;
import neyas.itmo.repository.CarRepository;
import neyas.itmo.repository.ModelRepository;
import neyas.itmo.repository.SparePartRepository;
import neyas.itmo.repository.UserRepository;
import neyas.itmo.specification.CarSpecifications;
import neyas.itmo.specification.ModelSpecifications;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
class RepositoryIT extends IntegrationTestBase {

    @Autowired
    private CarRepository carRepository;
    @Autowired
    private ModelRepository modelRepository;
    @Autowired
    private SparePartRepository sparePartRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void modelIsStoredWithValueObjectCode() {
        Model model = modelRepository.findByCode(new ModelId("BMW-320I")).orElseThrow();

        assertEquals("BMW", model.getManufacturer());
        assertEquals("320i", model.getName());
        assertEquals(3_380_000L, model.basePrice());
    }

    @Test
    void modelKeepsBaseConfigurationForEveryComponentType() {
        Model model = modelRepository.findByCode(new ModelId("BMW-320I")).orElseThrow();

        assertEquals(ComponentType.values().length, model.getBaseConfiguration().size());
        assertEquals("ENG-320I", model.baseComponent(ComponentType.ENGINE).getCode().code());
    }

    @Test
    void carKeepsComponentsAndComputesPrice() {
        Car car = carRepository.findAllByVin(new Vin("WBA8E9G50GNT12345")).orElseThrow();

        assertEquals(3_380_000L, car.price());
        assertEquals(FuelType.GASOLINE, car.fuelType());
        assertEquals(BodyType.SEDAN, car.bodyType());
        assertEquals(DriveType.REAR, car.driveType());
        assertEquals(TransmissionType.AUTOMATIC, car.transmissionType());
    }

    @Test
    void findsCarsByDerivedQueries() {
        assertEquals(2, carRepository.findAllByCarStatus(CarStatus.IN_STOCK).size());
        assertEquals(1, carRepository.findAllByCarStatus(CarStatus.ON_TEST_DRIVE).size());
        assertEquals(2, carRepository.findAllByModelCode(new ModelId("BMW-320I")).size());
        assertTrue(carRepository.existsByVin(new Vin("WBA8E9G50GNT12345")));
        assertFalse(carRepository.existsByVin(new Vin("WBA8E9G50GNT99999")));
    }

    @Test
    void specificationFiltersCarsByModelOnDatabaseLevel() {
        List<Car> cars = carRepository.findAll(CarSpecifications.matching(
                CarFilter.builder().manufacturer("BMW").model("330i").build()));

        assertEquals(1, cars.size());
        assertEquals("BMW-330I", cars.get(0).modelId().code());
    }

    @Test
    void specificationFiltersCarsByEngineAndStatus() {
        List<Car> cars = carRepository.findAll(CarSpecifications.matching(
                CarFilter.builder()
                        .carStatus(CarStatus.IN_STOCK)
                        .minPowerHp(200)
                        .build()));

        assertEquals(1, cars.size());
        assertEquals(258, cars.get(0).hpPower());
    }

    @Test
    void specificationFiltersCarsByPriceRange() {
        List<Car> cheap = carRepository.findAll(CarSpecifications.matching(
                CarFilter.builder().maxPrice(3_400_000L).build()));

        assertEquals(1, cheap.size());
        assertEquals(3_380_000L, cheap.get(0).price());
    }

    @Test
    void specificationFiltersModelsByManufacturer() {
        List<Model> models = modelRepository.findAll(ModelSpecifications.matching(
                ModelFilter.builder().manufacturer("bmw").build()));

        assertEquals(2, models.size());
    }

    @Test
    void specificationFiltersModelsByComponents() {
        List<Model> models = modelRepository.findAll(ModelSpecifications.matching(
                ModelFilter.builder()
                        .components(Set.of(new ComponentOptionId("INT-PERFORMANCE")))
                        .build()));

        assertEquals(1, models.size());
        assertEquals("BMW-330I", models.get(0).code().code());
    }

    @Test
    void sparePartKeepsCompatibleModels() {
        SparePart brakePads = sparePartRepository.findByCode(new SparePartId("SP-BRAKE-PADS")).orElseThrow();

        assertEquals(1, brakePads.getCompatibleModels().size());
        assertTrue(brakePads.fitsTo(new ModelId("BMW-320I")));
        assertFalse(brakePads.fitsTo(new ModelId("BMW-330I")));
    }

    @Test
    void findsSparePartsFittingToModel() {
        Model model = modelRepository.findByCode(new ModelId("BMW-330I")).orElseThrow();

        assertEquals(1, sparePartRepository.findAllByCompatibleModels(model).size());
    }

    @Test
    void userHierarchyIsStoredInSingleTable() {
        User manager = userRepository.findByEmail("manager@autosalon.ru").orElseThrow();

        assertNotNull(manager.getId());
        assertEquals(1, userRepository.findAllbyType(Manager.class).size());
        assertEquals(4, userRepository.findAll().size());
    }

    @Test
    void softDeleteKeepsRowAndHidesFromSpecification() {
        Car car = carRepository.findAllByVin(new Vin("WBA8E9G50GNT12347")).orElseThrow();
        car.markRemoved();
        carRepository.saveAndFlush(car);

        List<Car> visible = carRepository.findAll(CarSpecifications.matching(CarFilter.empty()));

        assertEquals(2, visible.size());
        assertEquals(3, carRepository.findAll().size());
    }

    @Test
    void auditFieldsAreFilledOnInsert() {
        Car car = carRepository.findAllByVin(new Vin("WBA8E9G50GNT12345")).orElseThrow();

        assertNotNull(car.getCreatedAt());
        assertNotNull(car.getUpdatedAt());
        assertFalse(car.isRemoved());
    }
}
