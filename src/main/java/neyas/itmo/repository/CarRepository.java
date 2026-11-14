package neyas.itmo.repository;

import neyas.itmo.entity.model.Model;
import neyas.itmo.entity.model.ModelId;
import neyas.itmo.entity.product.Car;
import neyas.itmo.entity.product.Vin;
import neyas.itmo.entity.type.CarStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CarRepository extends JpaRepository<Car, UUID>, JpaSpecificationExecutor<Car> {

    List<Car> findAllByCarStatus(CarStatus carStatus);
    List<Car> findAllByModelCode(ModelId modelCode);

    Optional<Car> findAllByVin(Vin vin);
    boolean existsByVin(Vin vin);


}
