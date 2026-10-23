package com.caleb.Creator_store.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "users")
@AllArgsConstructor 
@NoArgsConstructor 
@Builder 

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "First name can't be empty")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = "Last name can't be empty")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @NotBlank(message = "Email can't be empty")
    @Email(message = "Enter a valid email")
    @Column(name = "email", nullable = false)
    private String email;

    @NotBlank(message = "Phone number can't be empty")
    @Pattern(regexp = "0[17]\\d{8}", message = "Phone number must be 10 digits and start with 01 or 07")
    @Column(name = "phone", nullable = false)
    private String phoneNumber;


}
