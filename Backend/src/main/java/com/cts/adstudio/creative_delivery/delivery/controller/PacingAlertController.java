package com.cts.adstudio.creative_delivery.delivery.controller;

import com.cts.adstudio.creative_delivery.delivery.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pacing-alerts")
@RequiredArgsConstructor
public class PacingAlertController {

    private final AlertRepository repo;

    @GetMapping
    public Object getAll() {
        return repo.findAll();
    }
}