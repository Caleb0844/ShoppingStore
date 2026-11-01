package com.caleb.Creator_store.Services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.caleb.Creator_store.Entities.User;
import com.caleb.Creator_store.Repositories.UserRepo;
import com.caleb.Creator_store.dto.UserResponse;
import com.caleb.Creator_store.exceptions.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class UserService {

    private final UserRepo userRepo;
    public User createUser(User user){
        return userRepo.save(user);
    }
    public User updateUser(Long id,User user){
        User existingUser = userRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "User not found with id: " + id));

        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhoneNumber(user.getPhoneNumber());
            

        return userRepo.save(existingUser);
    }
    public void  deletUser(Long id, User user){
        userRepo.deleteById(id);
    }
    // Returns safe administrator user views without exposing password hashes or entity relationships.
    public List<UserResponse> getUsers(){
        return userRepo.findAll().stream()
                .map(this::toResponse)
                .toList();
    }
    public UserResponse getUserById(Long id,User user){
        return userRepo.findById(id)
        .map(this::toResponse)
        .orElseThrow(() -> new ResourceNotFoundException(
                "User not found with id: " + id));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .roles(user.getRoles())
                .build();
    }
    

}
