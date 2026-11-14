package neyas.itmo.service;

import lombok.RequiredArgsConstructor;
import neyas.itmo.entity.configuration.ComponentOption;
import neyas.itmo.entity.configuration.ComponentOptionId;
import neyas.itmo.entity.configuration.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import neyas.itmo.entity.model.Model;
import neyas.itmo.entity.model.ModelId;
import neyas.itmo.entity.product.Car;
import neyas.itmo.entity.type.ComponentType;
import neyas.itmo.exception.EntityNotFoundException;
import neyas.itmo.framework.NotNull;
import neyas.itmo.repository.CarRepository;
import neyas.itmo.repository.ModelRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarConfigurationService {
    @NotNull private final ModelRepository modelRepository;
    @NotNull private final CarRepository carRepository;

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public Specification configure(ModelId modelId, Map<ComponentType, ComponentOptionId> selectedOptions) {
        return Specification.of(requireModel(modelId), selectedOptions);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public Map<ComponentType, List<ComponentOption>> availableOptions(ModelId modelId) {
        Model model = requireModel(modelId);
        return Arrays.stream(ComponentType.values())
                .collect(Collectors.toMap(Function.identity(), model::optionOf,
                        (k,v) -> k, LinkedHashMap::new));
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public Specification baseSpecification(@NotNull ModelId modelId) {
        return Specification.base(requireModel(modelId));
    }

    @Transactional(readOnly = true)
    public long calculatePrice(ModelId modelId, Map<ComponentType, ComponentOptionId> selectedOptions) {
        Specification spec = Specification.of(requireModel(modelId), selectedOptions);
        return spec.price();
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
