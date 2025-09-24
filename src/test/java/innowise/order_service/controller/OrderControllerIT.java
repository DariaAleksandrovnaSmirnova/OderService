package innowise.order_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import innowise.order_service.client.UserServiceClient;
import innowise.order_service.dto.OrderItemRequestDto;
import innowise.order_service.dto.OrderRequestDto;
import innowise.order_service.dto.Status;
import innowise.order_service.dto.UpdateOrderDto;
import innowise.order_service.dto.client.UserDto;
import innowise.order_service.entity.Item;
import innowise.order_service.entity.Order;
import innowise.order_service.entity.OrderItem;
import innowise.order_service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class OrderControllerIT extends BaseIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserServiceClient userService;

    public static final Long ORDER_ID = 1L;
    public static final Long ITEM_ID = 1L;
    public static final Long USER_ID = 123L;
    public static final Integer QUANTITY = 2;

    private Order order;
    private OrderItemRequestDto orderItemRequestDto;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();

        Item item = Item.builder()
                .id(ITEM_ID)
                .name("Test Item")
                .price(50.0)
                .build();

        OrderItem orderItem = OrderItem.builder()
                .id(ITEM_ID)
                .item(item)
                .quantity(QUANTITY)
                .build();

        orderItemRequestDto = OrderItemRequestDto.builder()
                .itemId(ITEM_ID)
                .quantity(QUANTITY)
                .build();

        order = Order.builder()
                .id(ORDER_ID)
                .userId(USER_ID)
                .status(Status.PENDING)
                .orderItems(new ArrayList<>())
                .build();

        orderItem.setOrder(order);
        order.getOrderItems().add(orderItem);

        UserDto userDto = UserDto.builder()
                .id(USER_ID)
                .name("John")
                .surname("Doe")
                .email("john.doe@example.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();

        when(userService.getUserById(USER_ID)).thenReturn(userDto);
    }

    @Test
    void createOrder_ShouldReturnCreatedOrder() throws Exception {
        OrderRequestDto orderRequestDto = OrderRequestDto.builder()
                .userId(USER_ID)
                .orderItems(List.of(orderItemRequestDto))
                .build();

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.user.id").value(USER_ID))
                .andExpect(jsonPath("$.user.name").value("John"))
                .andExpect(jsonPath("$.orderItems.length()").value(1));
    }

    @Test
    void getOrderById_ShouldReturnOrder() throws Exception {
        Order savedOrder = orderRepository.save(order);

        mockMvc.perform(get("/orders/{id}", savedOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedOrder.getId()))
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.user.id").value(USER_ID));
    }

    @Test
    void getOrderById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/orders/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrdersByIds_ShouldReturnOrders() throws Exception {
        Order savedOrder1 = orderRepository.save(order);

        Order order2 = Order.builder()
                .userId(USER_ID)
                .status(Status.SUCCESS)
                .orderItems(new ArrayList<>())
                .build();
        Order savedOrder2 = orderRepository.save(order2);

        mockMvc.perform(post("/orders/list")
                        .param("orderIds",
                                savedOrder1.getId().toString(),
                                savedOrder2.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(savedOrder1.getId()))
                .andExpect(jsonPath("$[1].id").value(savedOrder2.getId()));
    }

    @Test
    void getOrdersByStatus_ShouldReturnFilteredOrders() throws Exception {
        Order pendingOrder = orderRepository.save(order);

        Order successOrder = Order.builder()
                .userId(USER_ID)
                .status(Status.SUCCESS)
                .orderItems(new ArrayList<>())
                .build();
        orderRepository.save(successOrder);

        mockMvc.perform(get("/orders/status/{status}", Status.PENDING))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].id").value(pendingOrder.getId()));
    }

    @Test
    void updateOrderById_ShouldUpdateAndReturnOrder() throws Exception {
        Order savedOrder = orderRepository.save(order);

        OrderItemRequestDto updatedOrderItem = OrderItemRequestDto.builder()
                .itemId(1L)
                .quantity(5)
                .build();

        UpdateOrderDto updateDto = UpdateOrderDto.builder()
                .orderItems(List.of(updatedOrderItem))
                .build();

        mockMvc.perform(put("/orders/{id}", savedOrder.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedOrder.getId()))
                .andExpect(jsonPath("$.orderItems.length()").value(1))
                .andExpect(jsonPath("$.orderItems[0].quantity").value(5));
    }

    @Test
    void deleteOrderById_ShouldDeleteOrder() throws Exception {
        Order savedOrder = orderRepository.save(order);

        mockMvc.perform(delete("/orders/{id}", savedOrder.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/orders/{id}", savedOrder.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void createOrder_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        OrderRequestDto invalidRequest = OrderRequestDto.builder()
                .userId(USER_ID)
                .orderItems(List.of())
                .build();

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_WithInvalidUserId_ShouldReturnBadRequest() throws Exception {
        OrderRequestDto invalidRequest = OrderRequestDto.builder()
                .userId(-1L)
                .orderItems(List.of(orderItemRequestDto))
                .build();

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}