package innowise.order_service.controller;


import innowise.order_service.dto.OrderRequestDto;
import innowise.order_service.dto.OrderResponseDto;
import innowise.order_service.dto.Status;
import innowise.order_service.dto.UpdateOrderDto;
import innowise.order_service.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    public final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody OrderRequestDto orderRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(orderRequestDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable @NotNull @Positive Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PostMapping("/list")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByIds(@RequestParam List<Long> orderIds) {
        return ResponseEntity.ok(orderService.getOrdersByIds(orderIds));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByStatus(@PathVariable Status status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponseDto> updateOrderById(@Valid @RequestBody UpdateOrderDto updateOrderDto,
                                                            @PathVariable @NotNull @Positive Long id) {
        return ResponseEntity.ok(orderService.updateOrderById(updateOrderDto, id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrderById(@PathVariable @NotNull @Positive Long id) {
        orderService.deleteOrderById(id);
    }
}
