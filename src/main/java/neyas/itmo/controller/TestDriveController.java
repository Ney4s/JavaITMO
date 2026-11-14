package neyas.itmo.controller;


import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import neyas.itmo.dto.OrderDto;
import neyas.itmo.dto.requests.CreateTestDriveRequest;
import neyas.itmo.mapper.DomainMapper;
import neyas.itmo.service.TestDriveService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/test-drives")
@RequiredArgsConstructor
public class TestDriveController {

    private final TestDriveService testDriveService;
    private final DomainMapper domainMapper;


    @PostMapping
    @Operation
    public OrderDto request(@RequestBody CreateTestDriveRequest testDriveRequest){
        return domainMapper.toDto(testDriveService.requestTestDrive(testDriveRequest.carId(), testDriveRequest.startsAt()));
    }

    @GetMapping
    @Operation
    public List<OrderDto> findAll(){
        return domainMapper.toOrderDtos(testDriveService.findAll());
    }

    @GetMapping("/cars/{carId}")
    @Operation
    public List<OrderDto> findForCar(@PathVariable UUID carId){
        return domainMapper.toOrderDtos(testDriveService.findAllForCar(carId));
    }


}
