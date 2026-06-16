package com.cts.adstudio.creative_delivery.delivery.controller;

import com.cts.adstudio.creative_delivery.delivery.dto.DeliveryRequest;
import com.cts.adstudio.creative_delivery.delivery.service.DeliveryService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService service;

    @PostMapping
    public ResponseEntity<?> simulate(@RequestBody DeliveryRequest req) {
        return ResponseEntity.ok(service.simulate(req));
    }
}