package neyas.itmo.entity.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import neyas.itmo.Validation;
import neyas.itmo.entity.BaseEntity;
import neyas.itmo.entity.Identifiable;
import neyas.itmo.entity.configuration.ComponentOption;
import neyas.itmo.entity.configuration.ComponentOptionId;
import neyas.itmo.entity.type.ComponentType;
import neyas.itmo.framework.NotBlank;
import neyas.itmo.framework.NotNull;

import java.util.*;


@Entity
@Getter
@Table(name = "models")
@NoArgsConstructor
public class Model extends BaseEntity {

    @Column(unique=true, nullable=false)
    private ModelId code;

    @Column(nullable=false)
    private String manufacturer;

    @Column(nullable=false)
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "model_base_configuration",
            joinColumns = @JoinColumn(name = "model_id"),
            inverseJoinColumns = @JoinColumn(name = "component_option_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "component_type")
    private Map<ComponentType, ComponentOption> baseConfiguration = new LinkedHashMap<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "model_available_options",
            joinColumns = @JoinColumn(name = "model_id"),
            inverseJoinColumns = @JoinColumn(name = "component_option_id"))
    private Set<ComponentOption> availableOptions = new LinkedHashSet<>();


    public Model(
            @NotNull ModelId code,
            @NotBlank String manufacturer,
            @NotBlank String name,
            @NotNull Map<ComponentType, ComponentOption> baseConfiguration,
            @NotNull Set<ComponentOption> availableOptions){
        validate(baseConfiguration, availableOptions);
        this.code = code;
        this.manufacturer = manufacturer;
        this.name = name;
        this.baseConfiguration = new  LinkedHashMap<>(baseConfiguration);
        this.availableOptions = new LinkedHashSet<>(availableOptions);
    }

    private static void validate(Map<ComponentType, ComponentOption> baseConfiguration,
                                 Set<ComponentOption> availableOptions) {
        Arrays.stream(ComponentType.values())
                .filter(type -> !baseConfiguration.containsKey(type))
                .findFirst()
                .ifPresent(type -> Validation.require(false,
                        "В базовой комплектации отсутствует обязательный узел «%s»"
                                .formatted(type.getDisplayName())));

        baseConfiguration.forEach((type, option) -> {
            Validation.requireNotNull(option, "базовый вариант узла «%s»".formatted(type.getDisplayName()));
            Validation.require(option.type() == type,
                    "Вариант «%s» относится к узлу «%s», а указан для узла «%s»"
                            .formatted(option.name(), option.type().getDisplayName(), type.getDisplayName()));
            Validation.require(availableOptions.contains(option),
                    "Базовый вариант «%s» отсутствует в списке вариантов модели".formatted(option.name()));
        });
    }

    public long surchargeOf(ComponentOption option) {
        return option.price() - baseComponent(option.type()).price();
    }

    public ModelId code(){
        return code;
    }

    public String manufacturer(){
        return manufacturer;
    }

    public String name(){
        return name;
    }

    public Map<ComponentType, ComponentOption> baseConfiguration(){
        return baseConfiguration;
    }

    public String fullName(){
        return "%s %s".formatted(manufacturer, name);
    }

    public long basePrice(){
        return baseConfiguration.values()
                .stream()
                .mapToLong(ComponentOption::price)
                .sum();
    }

    public Optional<ComponentOption> findOption(ComponentOptionId componentOptionId){
        return availableOptions.stream()
                .filter(option -> option.id().equals(componentOptionId))
                .findFirst();
    }

    public ComponentOption baseComponent(ComponentType componentType){
        return baseConfiguration.get(componentType);
    }

    public List<ComponentOption> optionOf(ComponentType componentType){
        return availableOptions.stream()
                .filter(option -> option.type() == componentType)
                .sorted(Comparator.comparingLong(ComponentOption::price))
                .toList();
    }
}
