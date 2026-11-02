package com.caleb.SecEx.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController 
@RequestMapping({"/", "/home"})

public class HelloController {
    @GetMapping
    public String greetings(){
        return "Welcome Caleb to spring security";
    }
    
}
