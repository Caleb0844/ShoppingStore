package com.caleb.SecEx.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.caleb.SecEx.Service.UserService;
import com.caleb.SecEx.model.Users;

@RestController
public class UserController {

    @Autowired 
    private  UserService service;

     @PostMapping("/register")
    public Users register(@RequestBody Users user){
        return service.register(user);
    }

    @PostMapping("/login")
    public String login(@RequestBody Users user){

        return service.verify(user);
    }

}
