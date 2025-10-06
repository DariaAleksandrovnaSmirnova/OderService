package innowise.order_service.dto;

import innowise.order_service.dto.client.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponseDto {
    private Long id;
    private Long userId;
    private Status status;
    private List<OrderItemRequestDto> orderItems;
    private UserDto user;
}
