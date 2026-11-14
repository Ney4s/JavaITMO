package neyas.itmo.controller;


import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import neyas.itmo.dto.SparePartDto;
import neyas.itmo.entity.model.ModelId;
import neyas.itmo.entity.product.SparePartId;
import neyas.itmo.mapper.DomainMapper;
import neyas.itmo.service.SparePartService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spare-parts")
@RequiredArgsConstructor
public class SparePartController {

    private final SparePartService sparePartService;
    private final DomainMapper domainMapper;

    @GetMapping
    @Operation
    public List<SparePartDto> findAll(@RequestParam(required = false) String model){
        return domainMapper.toSparePartDtos(model == null ?
                sparePartService.findAll() :
                sparePartService.findAllFittingTo(new ModelId(model))
                );
    }

    @GetMapping("/{code}")
    @Operation
    public SparePartDto findByCode(@PathVariable String code){
        return domainMapper.toDto(sparePartService.getByCode(new SparePartId(code)));
    }



}
