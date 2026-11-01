package com.caleb.Creator_store.dto;

import java.util.Set;

import com.caleb.Creator_store.Enums.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Defines the safe account data returned after registration or account lookup.
// Reuse this DTO instead of returning User directly so password and persistence relationships stay private.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Set<UserRole> roles;
}
