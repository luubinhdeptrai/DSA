package com.example.securitydemo.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/hello")
    public Map<String, String> hello(Principal principal) {
        return Map.of(
                "message", "hello from a USER-or-ADMIN endpoint",
                "username", principal.getName()
        );
    }
}
