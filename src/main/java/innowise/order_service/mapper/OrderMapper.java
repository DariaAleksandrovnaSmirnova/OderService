package innowise.order_service.mapper;

import innowise.order_service.dto.OrderItemRequestDto;
import innowise.order_service.dto.OrderRequestDto;
import innowise.order_service.dto.OrderResponseDto;
import innowise.order_service.entity.Order;
import innowise.order_service.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    @Mapping(target = "orderItems", source = "orderItems", qualifiedByName = "mapOrderItemsToDto")
    @Mapping(target = "user", ignore = true)
    OrderResponseDto toDto(Order order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Order toEntity(OrderRequestDto orderRequestDto);

    @Named("mapOrderItemsToDto")
    default List<OrderItemRequestDto> mapOrderItemsToDto(List<OrderItem> orderItems) {
        if (orderItems == null) {
            return Collections.emptyList();
        }
        return orderItems.stream()
                .map(this::toOrderItemDto)
                .toList();
    }

    default OrderItemRequestDto toOrderItemDto(OrderItem orderItem) {
        return OrderItemRequestDto.builder()
                .itemId(orderItem.getItem().getId())
                .quantity(orderItem.getQuantity())
                .build();
    }
}
