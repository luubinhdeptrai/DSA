package com.example.securitydemo.publicapi;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/public")
public class PublicController {

    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of("message", "hello from a public endpoint");
    }
}
