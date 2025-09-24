package innowise.order_service.service;

import innowise.order_service.client.UserServiceClient;
import innowise.order_service.dto.OrderItemRequestDto;
import innowise.order_service.dto.OrderRequestDto;
import innowise.order_service.dto.OrderResponseDto;
import innowise.order_service.dto.Status;
import innowise.order_service.dto.UpdateOrderDto;
import innowise.order_service.dto.client.UserDto;
import innowise.order_service.entity.Item;
import innowise.order_service.entity.Order;
import innowise.order_service.entity.OrderItem;
import innowise.order_service.mapper.OrderMapperImpl;
import innowise.order_service.repository.OrderItemRepository;
import innowise.order_service.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Spy
    private OrderMapperImpl orderMapper = new OrderMapperImpl();

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private ItemService itemService;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderService orderService;

    public static final Long ORDER_ID = 1L;
    public static final Long ITEM_ID = 1L;
    public static final Long WRONG_ORDER_ID = 999L;
    public static final Long USER_ID = 123L;
    public static final Integer QUANTITY = 2;

    private OrderRequestDto orderRequestDto;
    private UpdateOrderDto updateOrderDto;
    private Order order;
    private OrderResponseDto orderResponseDto;
    private Item item;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        item = Item.builder()
                .id(ITEM_ID)
                .build();

        OrderItem orderItem = OrderItem.builder()
                .id(ITEM_ID)
                .item(item)
                .quantity(QUANTITY)
                .build();

        OrderItemRequestDto orderItemRequestDto = OrderItemRequestDto.builder()
                .itemId(ITEM_ID)
                .quantity(QUANTITY)
                .build();

        order = Order.builder()
                .id(ORDER_ID)
                .userId(USER_ID)
                .status(Status.SUCCESS)
                .orderItems(new ArrayList<>(List.of(orderItem)))
                .build();

        orderRequestDto = OrderRequestDto.builder()
                .userId(USER_ID)
                .orderItems(List.of(orderItemRequestDto))
                .build();

        updateOrderDto = UpdateOrderDto.builder()
                .orderItems(List.of(orderItemRequestDto))
                .build();

        orderResponseDto = OrderResponseDto.builder()
                .id(ORDER_ID)
                .userId(USER_ID)
                .status(Status.SUCCESS)
                .build();

        userDto = UserDto.builder()
                .id(USER_ID)
                .name("John")
                .surname("Doe")
                .email("john.doe@example.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    void testCreateOrder() {
        when(itemService.getItemById(anyLong())).thenReturn(item);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(userServiceClient.getUserById(anyLong())).thenReturn(userDto);

        OrderResponseDto result = orderService.createOrder(orderRequestDto);

        assertNotNull(result);
        assertEquals(Status.SUCCESS, result.getStatus());
        verify(orderMapper).toEntity(orderRequestDto);
        verify(orderRepository).save(any(Order.class));
        verify(orderMapper).toDto(order);
    }

    @Test
    void testGetOrderById() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(userServiceClient.getUserById(anyLong())).thenReturn(userDto);

        OrderResponseDto result = orderService.getOrderById(ORDER_ID);

        assertNotNull(result);
        assertEquals(ORDER_ID, result.getId());
        verify(orderRepository).findById(ORDER_ID);
        verify(orderMapper).toDto(order);
    }

    @Test
    void testGetOrderByIdWithWrongId() {
        when(orderRepository.findById(WRONG_ORDER_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> orderService.getOrderById(WRONG_ORDER_ID));
        verify(orderRepository).findById(WRONG_ORDER_ID);
    }

    @Test
    void testGetOrdersByIds() {
        List<Long> orderIds = List.of(1L, 2L);
        Order order2 = new Order();
        order2.setId(2L);

        OrderResponseDto orderResponseDto2 = new OrderResponseDto();
        orderResponseDto2.setId(2L);

        when(orderRepository.findAllById(orderIds)).thenReturn(List.of(order, order2));
        when(userServiceClient.getUserById(anyLong())).thenReturn(userDto);

        List<OrderResponseDto> result = orderService.getOrdersByIds(orderIds);

        assertEquals(2, result.size());
        verify(orderRepository).findAllById(orderIds);
        verify(orderMapper, times(2)).toDto(any(Order.class));
    }

    @Test
    void testGetOrdersByStatus() {
        when(orderRepository.findByStatus(Status.SUCCESS)).thenReturn(List.of(order));
        when(userServiceClient.getUserById(anyLong())).thenReturn(userDto);

        List<OrderResponseDto> result = orderService.getOrdersByStatus(Status.SUCCESS);

        assertEquals(1, result.size());
        verify(orderRepository).findByStatus(Status.SUCCESS);
        verify(orderMapper).toDto(order);
    }

    @Test
    void testUpdateOrderById() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(itemService.getItemById(anyLong())).thenReturn(item);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(userServiceClient.getUserById(anyLong())).thenReturn(userDto);

        OrderResponseDto result = orderService.updateOrderById(updateOrderDto, ORDER_ID);

        assertNotNull(result);
        verify(orderRepository).save(order);
        verify(orderMapper).toDto(order);
    }

    @Test
    void testUpdateOrderByIdWithWrongId() {
        when(orderRepository.findById(WRONG_ORDER_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> orderService.updateOrderById(updateOrderDto, WRONG_ORDER_ID));
        verify(orderRepository).findById(WRONG_ORDER_ID);
    }

    @Test
    void testDeleteOrderById() {
        when(orderRepository.existsById(ORDER_ID)).thenReturn(true);

        orderService.deleteOrderById(ORDER_ID);

        verify(orderRepository).deleteById(ORDER_ID);
    }

    @Test
    void testDeleteOrderByIdWithWrongId() {
        when(orderRepository.existsById(WRONG_ORDER_ID)).thenReturn(false);

        assertThrows(EntityNotFoundException.class,
                () -> orderService.deleteOrderById(WRONG_ORDER_ID));
        verify(orderRepository, never()).deleteById(WRONG_ORDER_ID);
    }

    @Test
    void testDeleteOrderByIdWithNullId() {
        assertThrows(RuntimeException.class,
                () -> orderService.deleteOrderById(null));
        verify(orderRepository, never()).deleteById(any());
    }

    @Test
    void testAddUserInfoToOrderResponseWithFail() {
        when(userServiceClient.getUserById(anyLong())).thenThrow(new RuntimeException("Service unavailable"));

        OrderResponseDto result = orderService.addUserInfoToOrderResponse(orderResponseDto);

        assertNotNull(result);
        assertEquals(orderResponseDto.getId(), result.getId());
    }
}