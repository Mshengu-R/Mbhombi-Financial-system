package com.banking.frauddetectionservice.client;

import java.math.BigDecimal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient (name = "account-service", url = "http://localhost:8081")
public interface AccountServiceClient {
    
    @GetMapping("/api/v1/accounts/{accountNumber}/balance")
    BigDecimal getBalance (
        @PathVariable String accountNumber
    );
}
