package neyas.itmo.entity.configuration;

import neyas.itmo.entity.model.Model;
import neyas.itmo.entity.type.ComponentType;
import neyas.itmo.entity.type.FuelType;
import neyas.itmo.entity.type.TransmissionType;
import neyas.itmo.exception.DomainValidationException;
import neyas.itmo.exception.IncompatibleComponentException;
import neyas.itmo.framework.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public record Specification(
        @NotNull Map<ComponentType, ComponentOption> components
        ) {


    public Specification {
        components = Map.copyOf(components);
        validate(components);
    }

    private static void validate(Map<ComponentType, ComponentOption> components) {
        Arrays.stream(ComponentType.values())
                .filter(type -> components.get(type) == null)
                .findFirst()
                .ifPresent(type -> {
                    throw new DomainValidationException(
                            "Отсутствует обязательный узел «%s»".formatted(type.getDisplayName()));
                });

        components.forEach((type, option) -> {
            if (option.type() != type) {
                throw new DomainValidationException(
                        "Вариант «%s» относится к узлу «%s», а выбран для узла «%s»"
                                .formatted(option.name(), option.type().getDisplayName(), type.getDisplayName()));
            }
        });
    }

    public static Specification of(Model model, Map<ComponentType, ComponentOptionId> selection) {
        Map<ComponentType, ComponentOption> components = selection.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> resolve(model, entry.getValue())));
        return new Specification(components);
    }

    private static ComponentOption resolve(Model model, ComponentOptionId componentOptionId) {
        return model.findOption(componentOptionId).orElseThrow(() -> new IncompatibleComponentException(
                "Выбранный узел %s недоступен для модели %s".formatted(componentOptionId, model.fullName())));
    }

    public long surchargeOver(Model model) {
        return price() - model.basePrice();
    }



    public static Specification base(Model model) {
        return new Specification(model.baseConfiguration());
    }

    public long price(){
        return components.values()
                .stream()
                .mapToLong(ComponentOption::price)
                .sum();
    }

    public boolean isCustomFor(Model model){
        return !components.equals(model.baseConfiguration());
    }

    public BodyOption body(){
        return (BodyOption) components.get(ComponentType.BODY);
    }

    public DriveOption drive(){
        return (DriveOption) components.get(ComponentType.DRIVE);
    }

    public TransmissionOption transmission(){
        return (TransmissionOption) components.get(ComponentType.TRANSMISSION);
    }

    public FuelType fuelType(){
        return engine().fuelType();
    }

    public EngineOption engine(){
        return (EngineOption) components.get(ComponentType.ENGINE);
    }


}
