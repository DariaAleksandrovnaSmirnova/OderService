package innowise.order_service.service;

import innowise.order_service.entity.Item;
import innowise.order_service.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    public static final Long ITEM_ID = 1L;
    public static final Long WRONG_ITEM_ID = 999L;

    private Item item;

    @BeforeEach
    void setUp() {
        item = Item.builder()
                .id(ITEM_ID)
                .name("Test Item")
                .price(100.0)
                .build();
    }

    @Test
    void testGetItemById() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

        Item result = itemService.getItemById(ITEM_ID);

        assertNotNull(result);
        assertEquals(ITEM_ID, result.getId());
        assertEquals("Test Item", result.getName());
        verify(itemRepository).findById(ITEM_ID);
    }

    @Test
    void testGetItemByIdWithWrongId() {
        when(itemRepository.findById(WRONG_ITEM_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.getItemById(WRONG_ITEM_ID));
        verify(itemRepository).findById(WRONG_ITEM_ID);
    }

    @Test
    void testGetItemByIdWithNullId() {
        assertThrows(EntityNotFoundException.class, () -> itemService.getItemById(null));
        verify(itemRepository, never()).findById(anyLong());
    }
}