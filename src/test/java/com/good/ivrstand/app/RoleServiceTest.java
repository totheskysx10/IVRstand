package com.good.ivrstand.app;

import com.good.ivrstand.app.repository.RoleRepository;
import com.good.ivrstand.app.service.RoleService;
import com.good.ivrstand.domain.Role;
import com.good.ivrstand.domain.enumeration.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    @Test
    void testCheckRolesAndCreateIfNotExist() {
        when(roleRepository.existsByName(UserRole.ROLE_USER)).thenReturn(false);
        when(roleRepository.existsByName(UserRole.ROLE_ADMIN)).thenReturn(false);

        roleService.findRoleByName(UserRole.ROLE_USER);

        verify(roleRepository).save(argThat(role -> role.getName() == UserRole.ROLE_USER));
        verify(roleRepository).save(argThat(role -> role.getName() == UserRole.ROLE_ADMIN));
    }

    @Test
    void testCheckRolesNotCreateRolesAndCreateIfNotExistWhenExist() {
        when(roleRepository.existsByName(UserRole.ROLE_USER)).thenReturn(true);
        when(roleRepository.existsByName(UserRole.ROLE_ADMIN)).thenReturn(true);

        roleService.findRoleByName(UserRole.ROLE_USER);

        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void testFindRoleByName() {
        Role role = Role.builder().name(UserRole.ROLE_USER).build();
        when(roleRepository.existsByName(UserRole.ROLE_USER)).thenReturn(true);
        when(roleRepository.findByName(UserRole.ROLE_USER)).thenReturn(role);

        Role result = roleService.findRoleByName(UserRole.ROLE_USER);

        assertNotNull(result);
        assertEquals(UserRole.ROLE_USER, result.getName());
    }

    @Test
    void testFindRoleByNameCreatesMissingRoles() {
        Role role = Role.builder().name(UserRole.ROLE_USER).build();
        when(roleRepository.existsByName(UserRole.ROLE_USER)).thenReturn(false);
        when(roleRepository.existsByName(UserRole.ROLE_ADMIN)).thenReturn(false);
        when(roleRepository.findByName(UserRole.ROLE_USER)).thenReturn(role);

        Role result = roleService.findRoleByName(UserRole.ROLE_USER);

        assertNotNull(result);
        verify(roleRepository).save(argThat(r -> r.getName() == UserRole.ROLE_USER));
        verify(roleRepository).save(argThat(r -> r.getName() == UserRole.ROLE_ADMIN));
        verify(roleRepository).findByName(UserRole.ROLE_USER);
    }
}
