package com.banking.transactionservice.client;

import java.math.BigDecimal;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "account-service", url = "http://localhost:8081")
@EnableFeignClients
public interface AccountServiceClient {
    // when transaction is successful, we will deduct the amount from the sender's account
    @PutMapping("/api/v1/accounts/{accountNumber}/deduct")
    String deductBalance(
         @PathVariable("accountNumber") String accountNumber,
         @RequestParam("amount") BigDecimal amount);

         // for crediting back the reciver or the recipient when
    @PutMapping("/api/v1/accounts/{accountNumber}/credit")
    String creditBalance(
        @PathVariable("accountNumber") String accountNumber,
        @RequestParam("amount") BigDecimal amount
       );


       @GetMapping("/api/v1/accounts/{accountNumber}/balance")
       BigDecimal getBalance(@PathVariable String accountNumber);

}