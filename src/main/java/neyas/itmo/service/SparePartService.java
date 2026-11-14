package neyas.itmo.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import neyas.itmo.Validation;
import neyas.itmo.entity.model.Model;
import neyas.itmo.entity.model.ModelId;
import neyas.itmo.entity.product.Car;
import neyas.itmo.entity.product.SparePart;
import neyas.itmo.entity.product.SparePartId;
import neyas.itmo.exception.EntityNotFoundException;
import neyas.itmo.framework.NotNull;
import neyas.itmo.framework.PositiveOrZero;
import neyas.itmo.repository.CarRepository;
import neyas.itmo.repository.ModelRepository;
import neyas.itmo.repository.SparePartRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SparePartService {

    @NonNull private final SparePartRepository sparePartRepository;
    @NonNull private final ModelRepository modelRepository;
    @NonNull private final CarRepository carRepository;

    @PreAuthorize("hasRole('WAREHOUSE_ADMIN')")
    @Transactional
    public SparePart addSparePart(SparePart sparePart){
        Validation.require(!sparePartRepository.existsByCode(sparePart.getCode()),
                "Данная запчасть уже находится в системе. Возможно вы хотели изменить количество?");
        return sparePartRepository.save(sparePart);
    }

    @PreAuthorize("hasRole('WAREHOUSE_ADMIN')")
    @Transactional
    public SparePart changeQuantity(@NotNull SparePartId code,
                                    @PositiveOrZero int quantity){
        SparePart part = requireSparePart(code);
        part.changeQuantity(quantity);
        return part;
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public SparePart getByCode(@NonNull SparePartId code){
        return requireSparePart(code);
    }

    public List<SparePart> findAll(){
        return sparePartRepository.findAll();
    }

    public List<SparePart> findAllFittingTo(@NonNull ModelId modelId){
        Model model = requireModel(modelId);
        return sparePartRepository.findAllByCompatibleModels(model);
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

    SparePart requireSparePart(SparePartId code) {
        return sparePartRepository.findByCode(code)
                .orElseThrow(
                        () -> EntityNotFoundException.of(SparePart.class, code)
                );
    }


}
