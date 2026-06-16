package com.cts.adstudio.creative_delivery.delivery.deliveryexception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice(basePackages = "com.cts.adstudio.creative_delivery.delivery")
public class DeliveryExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handle(Exception ex) {

        ex.printStackTrace();

        return ResponseEntity.internalServerError()
                .body("Delivery Module Error: " + ex.getMessage());
    }
}