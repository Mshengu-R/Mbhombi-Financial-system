package com.banking.frauddetectionservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
// import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FraudCheckResults {
    
    public boolean fraud; // if fraudulent return true and not, returns false
    private String reason; // if fraudulent then reason


}
