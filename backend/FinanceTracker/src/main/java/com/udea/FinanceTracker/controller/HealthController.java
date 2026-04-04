package com.udea.FinanceTracker.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/")
    public String root() {
        return "FinanceTracker API - Funcionando correctamente";
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "FinanceTracker");
        status.put("timestamp", String.valueOf(System.currentTimeMillis()));
        status.put("message", "Service is healthy");
        return status;
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}