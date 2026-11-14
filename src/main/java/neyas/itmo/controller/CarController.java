package neyas.itmo.controller;


import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import neyas.itmo.dto.CarDto;
import neyas.itmo.dto.requests.AssignVinRequest;
import neyas.itmo.dto.requests.ChangeStatusRequest;
import neyas.itmo.entity.CarFilter;
import neyas.itmo.entity.product.Car;
import neyas.itmo.entity.product.Vin;
import neyas.itmo.entity.type.CarStatus;
import neyas.itmo.mapper.DomainMapper;
import neyas.itmo.service.CarCatalogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/cars")
@RequiredArgsConstructor
public class CarController {
    private final CarCatalogService carCatalogService;
    private final DomainMapper domainMapper;

    @GetMapping
    @Operation(summary = "Автомобили в наличии")
    public List<CarDto> findAvailable(CarFilter filter){
        return domainMapper.toCarDtos(carCatalogService.findAvailableCars(filter));
    }

    @GetMapping("/all")
    @Operation
    public List<CarDto> findAll(){
        return domainMapper.toCarDtos(carCatalogService.findAll());

    }

    @GetMapping("/testDriveCars")
    @Operation
    public List<CarDto> findTestDriveCars(){
        return domainMapper.toCarDtos(carCatalogService.getTestDriveCars());
    }

    @GetMapping("/{id}")
    @Operation
    public CarDto findById(@PathVariable UUID id){
        return domainMapper.toDto(carCatalogService.getCar(id));
    }

    @PatchMapping("/{id}/status")
    @Operation
    public CarDto changeStatus(@PathVariable UUID id, @RequestBody ChangeStatusRequest status){
        return domainMapper.toDto(carCatalogService.changeCarStatus(id, CarStatus.valueOf(status.status())));
    }

    @PostMapping("/{id}/vin")
    @Operation
    public CarDto assignVin(@PathVariable UUID id, @RequestBody AssignVinRequest vinRequest){
        return domainMapper.toDto(carCatalogService.assignVin(id, new Vin(vinRequest.vin())));
    }

    @PostMapping("/{id}/test-drive")
    @Operation
    public CarDto addToTestDriveCar(@PathVariable UUID id){
        return  domainMapper.toDto(carCatalogService.addToTestDriveCars(id));
    }

    @DeleteMapping("/{id}/test-drive")
    @Operation
    public CarDto removeFromTestDriveCar(@PathVariable UUID id){
        return domainMapper.toDto(carCatalogService.removeFromTestDriveCars(id));
    }
}
