package innowise.order_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemRequestDto {

    @NotNull(message = "Item ID cant be null")
    @Positive
    private Long itemId;

    @NotNull(message = "Quantity cant be null")
    @Positive
    private Integer quantity;
}
