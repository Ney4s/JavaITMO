package neyas.itmo.entity;

import neyas.itmo.Fixtures;
import neyas.itmo.entity.configuration.Specification;
import neyas.itmo.entity.product.Car;
import neyas.itmo.entity.product.Vin;
import neyas.itmo.entity.type.BodyType;
import neyas.itmo.entity.type.CarColor;
import neyas.itmo.entity.type.CarStatus;
import neyas.itmo.entity.type.DriveType;
import neyas.itmo.entity.type.FuelType;
import neyas.itmo.entity.type.TransmissionType;
import neyas.itmo.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CarTest {

    @Test
    void exposesSpecificationOfCar() {
        Car car = Fixtures.carInStock();

        assertEquals(3_380_000, car.price());
        assertEquals(FuelType.GASOLINE, car.fuelType());
        assertEquals(BodyType.SEDAN, car.bodyType());
        assertEquals(DriveType.REAR, car.driveType());
        assertEquals(TransmissionType.AUTOMATIC, car.transmissionType());
        assertEquals(184, car.hpPower());
        assertEquals(2.0, car.displacement());
    }

    @Test
    void carInStockIsAvailable() {
        Car car = Fixtures.carInStock();

        assertTrue(car.isAvailable());
        assertFalse(car.isOnTestDrive());

        car.changeStatus(CarStatus.ON_TEST_DRIVE);
        assertTrue(car.isOnTestDrive());
    }

    @Test
    void statusChangeKeepsIdentityOfCar() {
        Car car = Fixtures.carInStock();
        car.changeStatus(CarStatus.RESERVED);

        assertEquals(CarStatus.RESERVED, car.carStatus());
        assertEquals(Fixtures.VIN_BLACK, car.getVin());
        assertEquals(Fixtures.BMW_320I, car.modelId());
    }

    @Test
    void rejectsForbiddenStatusTransition() {
        Car car = Fixtures.carInStock();

        DomainValidationException exception = assertThrows(DomainValidationException.class,
                () -> car.changeStatus(CarStatus.DELIVERING));
        assertTrue(exception.getMessage().contains("Недопустимый переход"));
    }

    @Test
    void orderedCarHasNoVinUntilDelivery() {
        Car car = Fixtures.orderedCar();

        assertEquals(CarStatus.ORDERED, car.carStatus());
        assertNull(car.getVin());
        assertFalse(car.isAvailable());
    }

    @Test
    void orderedCarGoesThroughDeliveryToStock() {
        Car car = Fixtures.orderedCar();
        car.changeStatus(CarStatus.DELIVERING);
        car.assignVin(Fixtures.VIN_FREE);
        car.changeStatus(CarStatus.RESERVED);
        car.changeStatus(CarStatus.SOLD);

        assertEquals(CarStatus.SOLD, car.carStatus());
        assertEquals(Fixtures.VIN_FREE, car.getVin());
    }

    @Test
    void carInStockRequiresVin() {
        assertThrows(DomainValidationException.class,
                () -> new Car(Fixtures.bmw320i(), Specification.base(Fixtures.bmw320i()),
                        null, CarColor.WHITE, CarStatus.IN_STOCK));
    }

    @Test
    void vinIsAssignedOnce() {
        Car car = Fixtures.carInStock();

        assertThrows(DomainValidationException.class, () -> car.assignVin(Fixtures.VIN_FREE));
    }

    @Test
    void statusGraphIsClosedOnSoldAndCanceled() {
        assertTrue(CarStatus.SOLD.isTerminal());
        assertTrue(CarStatus.CANCELED.isTerminal());
        assertFalse(CarStatus.IN_STOCK.isTerminal());
        assertTrue(CarStatus.IN_STOCK.canChangeTo(CarStatus.ON_TEST_DRIVE));
        assertTrue(CarStatus.ON_TEST_DRIVE.canChangeTo(CarStatus.IN_STOCK));
        assertFalse(CarStatus.ON_TEST_DRIVE.canChangeTo(CarStatus.SOLD));
    }

    @Test
    void vinIsNormalizedAndChecked() {
        assertEquals("WBA3A5C50DF123456", new Vin(" wba3a5c50df123456 ").value());
        assertThrows(DomainValidationException.class, () -> new Vin("SHORT"));
        assertThrows(DomainValidationException.class, () -> new Vin("WBA3A5C50DF12345I"));
    }

    @Test
    void specificationIsRebuiltFromStoredComponents() {
        Car car = Fixtures.carInStock();

        assertEquals(Specification.base(Fixtures.bmw320i()).price(), car.specification().price());
        assertEquals(CarColor.BLACK, car.getColor());
    }
}
