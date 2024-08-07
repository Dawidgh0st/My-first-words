package pl.kasprzak.dawid.myfirstwords.service.parents;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.kasprzak.dawid.myfirstwords.exception.ParentNotFoundException;
import pl.kasprzak.dawid.myfirstwords.repository.ParentsRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class DeleteParentServiceTest {

    @Mock
    private ParentsRepository parentsRepository;
    @InjectMocks
    private DeleteParentService deleteParentService;

    /**
     * Unit test for deleteAccount method in DeleteParentService.
     * Verifies that a parent is removed when the parent ID exists.
     */
    @Test
    void when_deleteParent_then_parentShouldBeRemoved() {
        Long parentId = 1L;

        when(parentsRepository.existsById(parentId)).thenReturn(true);

        deleteParentService.deleteAccount(parentId);

        verify(parentsRepository, times(1)).existsById(parentId);
        verify(parentsRepository, times(1)).deleteById(parentId);

    }

    /**
     * Unit test for deleteAccount method in DeleteParentService.
     * Verifies that a ParentNotFoundException is thrown and the appropriate error message is returned,
     * when the parent ID does not exist.
     */
    @Test
    void when_deleteNonexistentParent_then_throwParentNotFoundException() {
        Long parentId = 1L;

        when(parentsRepository.existsById(parentId)).thenReturn(false);

        ParentNotFoundException parentNotFoundException = assertThrows(ParentNotFoundException.class, () -> deleteParentService.deleteAccount(parentId));

        assertEquals("Parent not found", parentNotFoundException.getMessage());
        verify(parentsRepository, times(1)).existsById(parentId);
        verify(parentsRepository, never()).deleteById(anyLong());
    }
}