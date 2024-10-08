package pl.kasprzak.dawid.myfirstwords.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.kasprzak.dawid.myfirstwords.exception.ChildNotFoundException;
import pl.kasprzak.dawid.myfirstwords.exception.ParentNotFoundException;
import pl.kasprzak.dawid.myfirstwords.repository.ChildrenRepository;
import pl.kasprzak.dawid.myfirstwords.repository.ParentsRepository;
import pl.kasprzak.dawid.myfirstwords.repository.dao.ChildEntity;
import pl.kasprzak.dawid.myfirstwords.repository.dao.ParentEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizationHelperTest {

    @Mock
    private ParentsRepository parentsRepository;
    @Mock
    private ChildrenRepository childrenRepository;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private AuthorizationHelper authorizationHelper;

    private ParentEntity parentEntity;
    private ChildEntity childEntity;

    @BeforeEach
    void setUp() {
        parentEntity = new ParentEntity();
        parentEntity.setId(1L);
        parentEntity.setUsername("parent");

        childEntity = new ChildEntity();
        childEntity.setId(1L);
        childEntity.setName("child");
        childEntity.setParent(parentEntity);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("parent");
    }

    /**
     * Unit test for validateAndAuthorizeChild method.
     * Verifies that the child is returned when the parent is authenticated and authorized using SecurityContextHolder.
     */
    @Test
    void when_validateAndAuthorizeChild_then_returnChild() {
        Long childId = childEntity.getId();

        when(parentsRepository.findByUsername("parent")).thenReturn(Optional.of(parentEntity));
        when(childrenRepository.findById(childId)).thenReturn(Optional.of(childEntity));

        ChildEntity result = authorizationHelper.validateAndAuthorizeChild(childId);

        assertEquals(childEntity, result);
        verify(parentsRepository, times(1)).findByUsername("parent");
        verify(childrenRepository, times(1)).findById(childId);
    }

    /**
     * Unit test for validateAndAuthorizeChild method.
     * Verifies that ParentNotFoundException is thrown when the parent is not found using SecurityContextHolder.
     */
    @Test
    void when_parentNotFound_then_throwParentNotFoundException() {
        Long childId = childEntity.getId();

        when(parentsRepository.findByUsername("parent")).thenReturn(Optional.empty());

        ParentNotFoundException parentNotFoundException = assertThrows(ParentNotFoundException.class,
                () -> authorizationHelper.validateAndAuthorizeChild(childId));

        assertEquals("Parent not found", parentNotFoundException.getMessage());
        verify(parentsRepository, times(1)).findByUsername("parent");
        verify(childrenRepository, never()).findById(anyLong());
    }

    /**
     * Unit test for validateAndAuthorizeChild method.
     * Verifies that ChildNotFoundException is thrown when the child is not found using SecurityContextHolder.
     */
    @Test
    void when_childNotFound_then_throwChildNotFoundException() {
        Long childId = childEntity.getId();

        when(parentsRepository.findByUsername("parent")).thenReturn(Optional.of(parentEntity));
        when(childrenRepository.findById(childId)).thenReturn(Optional.empty());

        ChildNotFoundException childNotFoundException = assertThrows(ChildNotFoundException.class,
                () -> authorizationHelper.validateAndAuthorizeChild(childId));

        assertEquals("Child not found", childNotFoundException.getMessage());
        verify(parentsRepository, times(1)).findByUsername("parent");
        verify(childrenRepository, times(1)).findById(childId);
    }

    /**
     * Unit test for validateAndAuthorizeChild method.
     * Verifies that AccessDeniedException is thrown when the child does not belong to the authenticated parent using SecurityContextHolder.
     */
    @Test
    void when_childDoesNotBelongToParent_then_throwAccessDeniedException() {
        Long childId = childEntity.getId();

        ParentEntity otherParent = new ParentEntity();
        otherParent.setId(2L);
        childEntity.setParent(otherParent);

        when(parentsRepository.findByUsername("parent")).thenReturn(Optional.of(parentEntity));
        when(childrenRepository.findById(childId)).thenReturn(Optional.of(childEntity));

        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class,
                () -> authorizationHelper.validateAndAuthorizeChild(childId));

        assertEquals("The parent does not have access to this child", accessDeniedException.getMessage());
        verify(parentsRepository, times(1)).findByUsername("parent");
        verify(childrenRepository, times(1)).findById(childId);
    }
}