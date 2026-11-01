package com.caleb.Creator_store.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.caleb.Creator_store.Entities.User;
import com.caleb.Creator_store.Enums.UserRole;
import com.caleb.Creator_store.Repositories.UserRepo;
import com.caleb.Creator_store.dto.UserRegistrationRequest;
import com.caleb.Creator_store.dto.UserResponse;
import com.caleb.Creator_store.exceptions.BusinessRuleException;

// Verifies secure account creation without requiring a database or HTTP server.
// Reuse this unit-test pattern for authentication service rules before adding endpoint tests.
@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegistrationService registrationService;

    // Confirms registration stores a hash, assigns CUSTOMER, and excludes the hash from the response.
    @Test
    void registerCreatesCustomerWithHashedPassword() {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .firstName(" Caleb ")
                .lastName(" Example ")
                .email(" CALEB@EXAMPLE.COM ")
                .phoneNumber("0712345678")
                .password("plain-password")
                .build();
        when(userRepo.existsByEmailIgnoreCase("caleb@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plain-password")).thenReturn("hashed-password");
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        UserResponse response = registrationService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("caleb@example.com", savedUser.getEmail());
        assertEquals("hashed-password", savedUser.getPasswordHash());
        assertEquals("Caleb", savedUser.getFirstName());
        assertEquals(UserRole.CUSTOMER, savedUser.getRoles().iterator().next());
        assertEquals(1L, response.getId());
        assertFalse(response.getRoles().contains(UserRole.SELLER));
    }

    // Confirms duplicate email registration is rejected before password hashing or persistence.
    @Test
    void registerRejectsDuplicateEmail() {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .email("caleb@example.com")
                .password("plain-password")
                .build();
        when(userRepo.existsByEmailIgnoreCase("caleb@example.com")).thenReturn(true);

        assertThrows(
                BusinessRuleException.class,
                () -> registrationService.register(request));

        verify(passwordEncoder, never()).encode(any());
        verify(userRepo, never()).save(any());
    }
}
