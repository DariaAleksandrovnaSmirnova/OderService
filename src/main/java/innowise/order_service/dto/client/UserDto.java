package innowise.order_service.dto.client;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record UserDto(
        Long id,
        String name,
        String surname,
        String email,
        LocalDate birthDate
) {
}
