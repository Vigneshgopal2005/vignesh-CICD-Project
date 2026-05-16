package com.abhishek;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "CI/CD Project Successfully Deployed on OpenShift 🚀";
    }
}
