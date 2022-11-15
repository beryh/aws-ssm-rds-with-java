package io.hochya.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HealthCheckController {
    @GetMapping
    public String healthCheckResponse() {
        return "Nothing here, used for health check. Try /mysfits instead.";
    }
}
