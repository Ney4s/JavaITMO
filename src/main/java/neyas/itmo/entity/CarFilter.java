package neyas.itmo.entity;

import lombok.Builder;
import neyas.itmo.Validation;
import neyas.itmo.entity.model.Model;
import neyas.itmo.entity.product.Car;
import neyas.itmo.entity.type.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Builder
public record CarFilter(
        Long minPrice,
        Long maxPrice,
        String manufacturer,
        String model,
        BodyType bodyType,
        FuelType fuelType,
        DriveType driveType,
        TransmissionType transmissionType,
        Integer minPowerHp,
        Integer maxPowerHp,
        Double minDisplacement,
        Double maxDisplacement,
        CarStatus carStatus) {
    public CarFilter {
        Validation.require(model == null || manufacturer != null,
                "Модель автомобиля можно выбрать только при выбранном бренде");
        Validation.require(minPrice == null || maxPrice == null || minPrice <= maxPrice,
                "Минимальная цена не может быть больше максимальной");
        Validation.require(minPowerHp == null || maxPowerHp == null || minPowerHp <= maxPowerHp,
                "Минимальная мощность не может быть больше максимальной");
        Validation.require(minDisplacement == null || maxDisplacement == null || minDisplacement <= maxDisplacement,
                "Минимальный объем двигателя не может быть больше максимального");
    }

    public boolean restrictsModel(){
        return manufacturer != null;
    }


    public boolean matchesModel(Model candidate) {
        if(manufacturer != null && !candidate.manufacturer().equalsIgnoreCase(manufacturer)){
            return false;
        }
        return model == null || candidate.name().equalsIgnoreCase(model);
    }

    public static CarFilter empty(){
        return CarFilter.builder().build();
    }

    public Predicate<Car> asPredicate(){
        List<Predicate<Car>> predicates = new ArrayList<>();

        if (minPrice != null) predicates.add(car -> car.price() >= minPrice);
        if (maxPrice != null) predicates.add(car -> car.price() <= maxPrice);
        if (minPowerHp != null) predicates.add(car -> car.hpPower() >= minPowerHp);
        if (maxPowerHp  != null) predicates.add(car -> car.hpPower() <= maxPowerHp);
        if (minDisplacement != null) predicates.add(car -> car.displacement() >= minDisplacement);
        if (maxDisplacement != null) predicates.add(car -> car.displacement() <= maxDisplacement);
        if (bodyType != null) predicates.add(car -> car.bodyType() == bodyType);
        if (fuelType != null) predicates.add(car -> car.fuelType() == fuelType);
        if (carStatus != null) predicates.add(car -> car.carStatus() == carStatus);
        if (transmissionType != null) predicates.add(car -> car.transmissionType() == transmissionType);
        if (driveType != null) predicates.add(car -> car.driveType() == driveType);

        return predicates.stream().reduce(Predicate::and).orElse(car -> true);
    }


}
