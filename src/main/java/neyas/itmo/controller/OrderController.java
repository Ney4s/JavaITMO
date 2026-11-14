package neyas.itmo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import neyas.itmo.dto.OrderDto;
import neyas.itmo.dto.requests.ChangeStatusRequest;
import neyas.itmo.dto.requests.CreateCustomOrderRequest;
import neyas.itmo.dto.requests.CreatePurchaseOrderRequest;
import neyas.itmo.dto.requests.CreateSparePartOrderRequest;
import neyas.itmo.entity.configuration.ComponentOptionId;
import neyas.itmo.entity.configuration.Specification;
import neyas.itmo.entity.model.ModelId;
import neyas.itmo.entity.order.OrderStatus;
import neyas.itmo.entity.product.SparePartId;
import neyas.itmo.entity.type.CarColor;
import neyas.itmo.entity.type.ComponentType;
import neyas.itmo.entity.type.OrderType;
import neyas.itmo.mapper.DomainMapper;
import neyas.itmo.service.CarConfigurationService;
import neyas.itmo.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Заказы")
public class OrderController {

    private final OrderService orderService;
    private final CarConfigurationService carConfigurationService;
    private final DomainMapper domainMapper;

    @PostMapping("/purchase")
    @Operation(summary = "Заказ автомобиля в наличии")
    public OrderDto createPurchaseOrder(@RequestBody CreatePurchaseOrderRequest request){
        return domainMapper.toDto(orderService.createPurchaseOrder(request.carId()));
    }

    @PostMapping("/custom")
    @Operation(summary = "Заказ автомобиля с определенной комплектацией")
    public OrderDto createCustomOrder(@RequestBody CreateCustomOrderRequest request){
        ModelId modelId = new ModelId(request.modelCode());
        Specification specification = carConfigurationService.configure(modelId, selectionOf(request));
        return domainMapper.toDto(orderService.createCustomOrder(
                modelId, specification, CarColor.valueOf(request.color())));
    }

    @PostMapping("/spare-parts")
    @Operation(summary = "Заказ запчасти")
    public OrderDto createSparePartOrder(@RequestBody CreateSparePartOrderRequest request){
        return domainMapper.toDto(orderService.createSparePartOrder(
                new SparePartId(request.sparePartCode()), request.quantity()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Заказ по идентификатору")
    public OrderDto findById(@PathVariable UUID id){
        return domainMapper.toDto(orderService.findById(id));
    }

    @GetMapping
    @Operation(summary = "Заказы по типу")
    public List<OrderDto> findAllByType(@RequestParam OrderType type){
        return domainMapper.toOrderDtos(orderService.findAllByType(type));
    }

    @GetMapping("/my")
    @Operation(summary = "Заказы текущего пользователя")
    public List<OrderDto> findMyOrders(){
        return domainMapper.toOrderDtos(orderService.findMyOrders());
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Смена статуса заказа")
    public OrderDto updateStatus(@PathVariable UUID id, @RequestBody ChangeStatusRequest request){
        return domainMapper.toDto(orderService.updateStatus(id, OrderStatus.valueOf(request.status())));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Отмена заказа")
    public OrderDto cancel(@PathVariable UUID id){
        return domainMapper.toDto(orderService.cancel(id));
    }

    private static Map<ComponentType, ComponentOptionId> selectionOf(CreateCustomOrderRequest request){
        return request.options()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> new ComponentOptionId(entry.getValue())));
    }
}
