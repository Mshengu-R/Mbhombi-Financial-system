package com.banking.paymentservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "payfast")
public class PayFastProperties {

    private String merchantId;
    private String merchantKey;
    private String passphrase;
    private String returnUrl;
    private String cancelUrl;
    private String notifyUrl;
    private String sandboxUrl = "https://sandbox.payfast.co.za/eng/process";
    private String currency = "ZAR";
}
