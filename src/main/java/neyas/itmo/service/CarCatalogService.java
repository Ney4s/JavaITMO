package neyas.itmo.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import neyas.itmo.Validation;
import neyas.itmo.entity.CarFilter;
import neyas.itmo.entity.ModelFilter;
import neyas.itmo.entity.model.Model;
import neyas.itmo.entity.model.ModelId;
import neyas.itmo.entity.product.Car;
import neyas.itmo.entity.product.Vin;
import neyas.itmo.entity.type.CarStatus;
import neyas.itmo.exception.EntityNotFoundException;
import neyas.itmo.framework.NotNull;
import neyas.itmo.repository.CarRepository;
import neyas.itmo.repository.ModelRepository;
import neyas.itmo.specification.CarSpecifications;
import neyas.itmo.specification.ModelSpecifications;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CarCatalogService {
    @NonNull private final ModelRepository modelRepository;
    @NotNull private final CarRepository carRepository;


    @PreAuthorize("hasRole('WAREHOUSE_ADMIN')")
    @Transactional
    public Car addCar(@NotNull Car car) {
        requireModel(car.modelId());
        if(car.getVin() != null) {
            Validation.require(!carRepository.existsByVin(car.getVin()),
                    "Автомобиль с данным VIN(%s) номером уже есть в системе".formatted(car.getVin()));
        }
        return carRepository.save(car);
    }

    @PreAuthorize("hasRole('WAREHOUSE_ADMIN')")
    @Transactional
    public void removeCar(@NotNull UUID carId) {
        Car car = requireCar(carId);
        Validation.require(car.getCarStatus() == CarStatus.CANCELED,
                "Удалить можно только автомобиль с отмененныи заказом");
        car.markRemoved();
    }

    @PreAuthorize("hasRole('WAREHOUSE_ADMIN')")
    @Transactional
    public Car changeCarStatus(@NotNull UUID carId,
                               @NotNull CarStatus newStatus) {
        return applyStatus(carId, newStatus);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public Car getCar(@NotNull UUID carId) {
        return requireCar(carId);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public Model getModel(@NotNull ModelId modelId) {
        return requireModel(modelId);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<Model> findModels(CarFilter filter) {
        return findModels(ModelFilter.of(filter));
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<Model> findModels(ModelFilter filter) {
        return modelRepository.findAll(ModelSpecifications.matching(filter));
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<Car> findAll(){
        return carRepository.findAll();
    }


    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<Car> findAvailableCars(CarFilter filter) {
        return carRepository.findAll(CarSpecifications.matching(filter));
    }

    @PreAuthorize("hasAnyRole('USER','MANAGER')")
    @Transactional(readOnly = true)
    public List<Car> getTestDriveCars(){
        return carRepository.findAllByCarStatus(CarStatus.ON_TEST_DRIVE);
    }

    @PreAuthorize("hasRole('WAREHOUSE_ADMIN')")
    @Transactional
    public Car assignVin(@NotNull UUID carId,
                         @NotNull Vin vin) {
        Validation.require(!carRepository.existsByVin(vin),
                "Автомобиль с VIN %s уже есть в системе".formatted(vin));
        Car car = requireCar(carId);
        car.assignVin(vin);
        return car;
    }

    @PreAuthorize("hasRole('WAREHOUSE_ADMIN')")
    @Transactional
    public Car addToTestDriveCars(@NotNull UUID carId){
        return applyStatus(carId, CarStatus.ON_TEST_DRIVE);
    }

    @PreAuthorize("hasRole('WAREHOUSE_ADMIN')")
    @Transactional
    public Car removeFromTestDriveCars(@NotNull UUID carId) {
        return applyStatus(carId, CarStatus.IN_STOCK);
    }


    Car applyStatus(@NonNull UUID carId, CarStatus newStatus) {
        Car car = requireCar(carId);
        car.changeStatus(newStatus);
        return car;
    }

    Car requireCar(UUID carId) {
        return carRepository.findById(carId)
                .orElseThrow(
                        () -> EntityNotFoundException.of(Car.class, carId));
    }

    Model requireModel(ModelId modelId) {
        return modelRepository.findByCode(modelId)
                .orElseThrow(
                        () -> EntityNotFoundException.of(Model.class, modelId));
    }
}
