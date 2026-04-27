package com.workstudy.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "✅ Backend is running successfully on Render 🚀";
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}