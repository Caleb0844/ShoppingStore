package com.caleb.SecEx.Controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.caleb.SecEx.oders.Student;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping
public class StudentController {

    private List<Student> students = new ArrayList<>(List.of(
        new Student(1, "Caleb", 85.5f),
        new Student(2, "Amina", 91.0f),
        new Student(3, "Brian", 78.5f),
        new Student(4, "Diana", 88.0f),
        new Student(5, "Ethan", 94.5f)
    ));

    @GetMapping("/students")
    public List<Student> getStudents(){
        return students;
    }

    @GetMapping ("/csrftkn")
    public CsrfToken getToken(HttpServletRequest request){
        return (CsrfToken) request.getAttribute("_csrf");
    }

    @PostMapping ("/students")
    public Student addStudent(@RequestBody Student student){
        students.add(student);
        return student;
    }
}
