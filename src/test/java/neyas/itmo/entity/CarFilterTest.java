package neyas.itmo.entity;

import neyas.itmo.Fixtures;
import neyas.itmo.entity.product.Car;
import neyas.itmo.entity.type.BodyType;
import neyas.itmo.entity.type.CarStatus;
import neyas.itmo.entity.type.DriveType;
import neyas.itmo.entity.type.FuelType;
import neyas.itmo.entity.type.TransmissionType;
import neyas.itmo.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CarFilterTest {

    private final List<Car> cars = List.of(Fixtures.carInStock(), onTestDrive());

    private static Car onTestDrive() {
        Car car = Fixtures.carInStock(Fixtures.VIN_WHITE);
        car.changeStatus(CarStatus.ON_TEST_DRIVE);
        return car;
    }

    private long matched(CarFilter filter) {
        return cars.stream().filter(filter.asPredicate()).count();
    }

    @Test
    void emptyFilterMatchesEverything() {
        assertEquals(2, matched(CarFilter.empty()));
    }

    @Test
    void filtersByPrice() {
        assertEquals(2, matched(CarFilter.builder().maxPrice(3_380_000L).build()));
        assertEquals(0, matched(CarFilter.builder().minPrice(3_380_001L).build()));
        assertEquals(2, matched(CarFilter.builder().minPrice(1L).maxPrice(4_000_000L).build()));
    }

    @Test
    void filtersByEngineCharacteristics() {
        assertEquals(2, matched(CarFilter.builder().fuelType(FuelType.GASOLINE).build()));
        assertEquals(0, matched(CarFilter.builder().fuelType(FuelType.DIESEL).build()));
        assertEquals(2, matched(CarFilter.builder().minPowerHp(100).maxPowerHp(200).build()));
        assertEquals(0, matched(CarFilter.builder().minPowerHp(200).build()));
        assertEquals(2, matched(CarFilter.builder().maxDisplacement(2.5).build()));
        assertEquals(0, matched(CarFilter.builder().minDisplacement(2.5).build()));
    }

    @Test
    void filtersByBodyDriveAndTransmission() {
        assertEquals(2, matched(CarFilter.builder().bodyType(BodyType.SEDAN).build()));
        assertEquals(0, matched(CarFilter.builder().bodyType(BodyType.COUPE).build()));
        assertEquals(2, matched(CarFilter.builder().driveType(DriveType.REAR).build()));
        assertEquals(2, matched(CarFilter.builder().transmissionType(TransmissionType.AUTOMATIC).build()));
        assertEquals(0, matched(CarFilter.builder().transmissionType(TransmissionType.MANUAL).build()));
    }

    @Test
    void filtersByStatus() {
        assertEquals(1, matched(CarFilter.builder().carStatus(CarStatus.IN_STOCK).build()));
        assertEquals(1, matched(CarFilter.builder().carStatus(CarStatus.ON_TEST_DRIVE).build()));
        assertEquals(0, matched(CarFilter.builder().carStatus(CarStatus.SOLD).build()));
    }

    @Test
    void matchesModelByManufacturerAndName() {
        CarFilter filter = CarFilter.builder().manufacturer("bmw").model("320i").build();

        assertTrue(filter.restrictsModel());
        assertTrue(filter.matchesModel(Fixtures.bmw320i()));
        assertFalse(filter.matchesModel(Fixtures.bmw330i()));
        assertFalse(CarFilter.empty().restrictsModel());
    }

    @Test
    void modelRequiresManufacturer() {
        assertThrows(DomainValidationException.class, () -> CarFilter.builder().model("320i").build());
    }

    @Test
    void rejectsInvertedRanges() {
        assertThrows(DomainValidationException.class,
                () -> CarFilter.builder().minPrice(2L).maxPrice(1L).build());
        assertThrows(DomainValidationException.class,
                () -> CarFilter.builder().minPowerHp(300).maxPowerHp(100).build());
        assertThrows(DomainValidationException.class,
                () -> CarFilter.builder().minDisplacement(3.0).maxDisplacement(2.0).build());
    }
}
