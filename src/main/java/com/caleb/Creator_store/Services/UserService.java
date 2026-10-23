package com.caleb.Creator_store.Services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.caleb.Creator_store.Entities.User;
import com.caleb.Creator_store.Repositories.UserRepo;

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
            .orElseThrow(() ->new RuntimeException("User not found with id: "+id));

        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhoneNumber(user.getPhoneNumber());
            

        return userRepo.save(existingUser);
    }
    public User deletUser(Long id, User user){
        return null;
    }
    public List <User> getUsers(){
        return null;
    }
    public User getUserById(Long id,User user){
        return null; 
    }
    

}
