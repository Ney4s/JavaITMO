package neyas.itmo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import neyas.itmo.dto.ComponentOptionDto;
import neyas.itmo.dto.SpecificationDto;
import neyas.itmo.entity.configuration.ComponentOption;
import neyas.itmo.entity.configuration.ComponentOptionId;
import neyas.itmo.entity.model.ModelId;
import neyas.itmo.entity.type.ComponentType;
import neyas.itmo.mapper.DomainMapper;
import neyas.itmo.service.CarConfigurationService;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/models/{code}/configuration")
@RequiredArgsConstructor
@Tag(name = "Конфигуратор")
public class ConfigurationController {

    private final CarConfigurationService carConfigurationService;
    private final DomainMapper domainMapper;

    @GetMapping
    @Operation(summary = "Базовая комплектация модели")
    public SpecificationDto baseSpecification(@PathVariable String code){
        return domainMapper.toDto(carConfigurationService.baseSpecification(new ModelId(code)));
    }

    @GetMapping("/options")
    @Operation(summary = "Доступные варианты узлов")
    public Map<String, List<ComponentOptionDto>> availableOptions(@PathVariable String code){
        Map<ComponentType, List<ComponentOption>> options =
                carConfigurationService.availableOptions(new ModelId(code));

        Map<String, List<ComponentOptionDto>> result = new LinkedHashMap<>();
        options.forEach((type, values) -> result.put(type.name(), values.stream()
                .map(domainMapper::toDto)
                .toList()));
        return result;
    }

    @PostMapping
    @Operation(summary = "Сборка комплектации и расчет стоимости")
    public SpecificationDto configure(@PathVariable String code,
                                      @RequestBody Map<ComponentType, String> selection){
        return domainMapper.toDto(carConfigurationService.configure(new ModelId(code), selectionOf(selection)));
    }

    private static Map<ComponentType, ComponentOptionId> selectionOf(Map<ComponentType, String> selection){
        return selection.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> new ComponentOptionId(entry.getValue())));
    }
}
