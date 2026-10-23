package com.caleb.Creator_store.Controllers;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.caleb.Creator_store.Entities.User;
import com.caleb.Creator_store.Services.UserService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping ("/api/user")
@RequiredArgsConstructor 

public class UserController {

    private final UserService userService;
    @PostMapping 
    public User createUser(@Valid @RequestBody User user){
        return userService.createUser(user);
    }
    @PutMapping ("/{id")
    public User updateUser(@PathVariable Long id,@Valid @RequestBody  User user){
        return userService.updateUser(id, user);
    }
    @DeleteMapping ("/{id}")
    public void  deletUser(@PathVariable Long id, User user){
        userService.deletUser(id, user);
    }
    @GetMapping 
    public List <User> getUsers(User user){
        return userService.getUsers();
    }
    @GetMapping ("/{id}")
    public User getUserById(@PathVariable Long id,User user){
        return userService.getUserById(id, user); 
    }
    

}
