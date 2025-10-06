package innowise.order_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ItemDto {

    @NotNull(message = "ID cant be null")
    @Positive
    private Long id;

    @NotBlank(message = "Name can't be blank")
    private String name;

    @NotBlank(message = "Price can't be blank")
    private Double price;
}
