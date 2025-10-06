package innowise.order_service.service;

import innowise.order_service.client.UserServiceClient;
import innowise.order_service.dto.OrderItemRequestDto;
import innowise.order_service.dto.OrderRequestDto;
import innowise.order_service.dto.OrderResponseDto;
import innowise.order_service.dto.Status;
import innowise.order_service.dto.UpdateOrderDto;
import innowise.order_service.entity.Order;
import innowise.order_service.entity.OrderItem;
import innowise.order_service.mapper.OrderMapper;
import innowise.order_service.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    public final OrderRepository orderRepository;
    public final OrderMapper orderMapper;
    public final UserServiceClient userServiceClient;
    public final ItemService itemService;

    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto orderRequestDto) {
        Order order = orderMapper.toEntity(orderRequestDto);
        order.setOrderItems(getOrderItems(order, orderRequestDto.getOrderItems()));
        order.setStatus(Status.SUCCESS);
        Order updatedOrder = orderRepository.save(order);
        return addUserInfoToOrderResponse(orderMapper.toDto(updatedOrder));
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long id) {
        OrderResponseDto orderResponseDto = orderRepository.findById(id)
                .map(orderMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException(String.format("There is no order with id %d", id)));
        return addUserInfoToOrderResponse(orderResponseDto);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByIds(List<Long> orderIds) {
        return orderRepository.findAllById(orderIds).stream()
                .map(orderMapper::toDto)
                .map(this::addUserInfoToOrderResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByStatus(Status status) {
        return orderRepository.findByStatus(status).stream()
                .map(orderMapper::toDto)
                .map(this::addUserInfoToOrderResponse)
                .toList();
    }

    @Transactional
    public OrderResponseDto updateOrderById(UpdateOrderDto updateOrderDto, Long id) {
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("There is no order with id %d", id)));

        existingOrder.getOrderItems().clear();
        List<OrderItem> newOrderItems = getOrderItems(existingOrder, updateOrderDto.getOrderItems());
        existingOrder.getOrderItems().addAll(newOrderItems);
        existingOrder.setStatus(Status.SUCCESS);
        Order updatedOrder = orderRepository.save(existingOrder);
        return addUserInfoToOrderResponse(orderMapper.toDto(updatedOrder));
    }

    @Transactional
    public void deleteOrderById(Long id) {
        validateOrderId(id);
        orderRepository.deleteById(id);
    }

    private void validateOrderId(Long id) {
        if (id == null) {
            throw new RuntimeException("ID can't be null");
        }
        if (!orderRepository.existsById(id)) {
            throw new EntityNotFoundException(String.format("There is no order with id %d", id));

        }
    }

    protected List<OrderItem> getOrderItems(Order order, List<OrderItemRequestDto> orderItems) {
        return orderItems.stream()
                .map(item -> OrderItem.builder()
                        .order(order)
                        .item(itemService.getItemById(item.getItemId()))
                        .quantity(item.getQuantity())
                        .build())
                .toList();
    }

    protected OrderResponseDto addUserInfoToOrderResponse(OrderResponseDto orderResponseDto) {
        try {
            orderResponseDto.setUser(userServiceClient.getUserById(orderResponseDto.getUserId()));
        } catch (Exception e) {
            log.warn("Failed to fetch user info for userId: {}", orderResponseDto.getUserId(), e);
        }
        return orderResponseDto;
    }
}
