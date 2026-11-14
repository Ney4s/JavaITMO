package neyas.itmo.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import neyas.itmo.dto.ModelDto;
import neyas.itmo.entity.ModelFilter;
import neyas.itmo.entity.model.ModelId;
import neyas.itmo.mapper.DomainMapper;
import neyas.itmo.repository.CarRepository;
import neyas.itmo.service.CarCatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
@Tag(name = "Модели")
public class ModelController {

    private final CarCatalogService carCatalogService;
    private final DomainMapper domainMapper;

    @GetMapping
    @Operation
    public List<ModelDto> findAll(ModelFilter filter){
        return domainMapper.toModelDtos(carCatalogService.findModels(filter));
    }

    @GetMapping("/{code}")
    @Operation
    public ModelDto findByCode(@PathVariable String code){
        return domainMapper.toDto(carCatalogService.getModel(new ModelId(code)));
    }



}
