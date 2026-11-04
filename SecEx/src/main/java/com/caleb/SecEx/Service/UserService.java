package com.caleb.SecEx.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.caleb.SecEx.Repository.UserRepo;
import com.caleb.SecEx.model.Users;

@Service 
public class UserService {

    @Autowired 
    private UserRepo repo;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

   
    public Users register(Users user){
        user.setPassword(encoder.encode(user.getPassword()));
       return repo.save(user);
        
    }

}
