package com.banking.paymentservice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.banking.paymentservice.config.PayFastProperties;
import com.banking.paymentservice.dto.CreatePaymentRequest;
import com.banking.paymentservice.dto.PaymentOrderResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayFastService {

    private final PayFastProperties payFastProperties;

    @Value("${payfast.sandbox-url:https://sandbox.payfast.co.za/eng/process}")
    private String sandboxUrl;

    public PaymentOrderResponse createPaymentOrder(CreatePaymentRequest request) {
        String paymentId = UUID.randomUUID().toString();
        String merchantId = payFastProperties.getMerchantId();
        String merchantKey = payFastProperties.getMerchantKey();
        String passphrase = payFastProperties.getPassphrase();

        if (merchantId == null || merchantKey == null || passphrase == null) {
            throw new IllegalStateException("PayFast credentials are not configured");
        }

        BigDecimal amount = request.getAmount().setScale(2, RoundingMode.HALF_UP);
        String currency = request.getCurrency() == null || request.getCurrency().isBlank() ? payFastProperties.getCurrency() : request.getCurrency();

        Map<String, String> params = new LinkedHashMap<>();
        params.put("merchant_id", merchantId);
        params.put("merchant_key", merchantKey);
        params.put("return_url", payFastProperties.getReturnUrl());
        params.put("cancel_url", payFastProperties.getCancelUrl());
        params.put("notify_url", payFastProperties.getNotifyUrl());
        params.put("name_first", "Customer");
        params.put("name_last", "User");
        params.put("email_address", "customer@example.com");
        params.put("m_payment_id", paymentId);
        params.put("amount", amount.toPlainString());
        params.put("item_name", request.getDescription() == null ? "Payment" : request.getDescription());
        params.put("item_description", request.getDescription() == null ? "Banking payment" : request.getDescription());
        params.put("currency", currency);
        params.put("subscription_type", "1");

        String signature = generateSignature(params, passphrase);

        String baseUrl = (sandboxUrl == null || sandboxUrl.isBlank())
                ? "https://sandbox.payfast.co.za/eng/process"
                : sandboxUrl;

        String paymentUrl = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("merchant_id", merchantId)
                .queryParam("merchant_key", merchantKey)
                .queryParam("return_url", payFastProperties.getReturnUrl())
                .queryParam("cancel_url", payFastProperties.getCancelUrl())
                .queryParam("notify_url", payFastProperties.getNotifyUrl())
                .queryParam("name_first", "Customer")
                .queryParam("name_last", "User")
                .queryParam("email_address", "customer@example.com")
                .queryParam("m_payment_id", paymentId)
                .queryParam("amount", amount.toPlainString())
                .queryParam("item_name", request.getDescription() == null ? "Payment" : request.getDescription())
                .queryParam("item_description", request.getDescription() == null ? "Banking payment" : request.getDescription())
                .queryParam("currency", currency)
                .queryParam("signature", signature)
                .build(true)
                .toUriString();

        PaymentOrderResponse response = new PaymentOrderResponse();
        response.setId(paymentId);
        response.setPayFastOderId(paymentId);
        response.setPayFastId(paymentId);
        response.setPayFastKeyId(merchantKey);
        response.setAccountNumber(request.getAccountNumber());
        response.setAmount(amount);
        response.setCurrency(currency);
        response.setStatus("CREATED");
        response.setDescription(request.getDescription());
        response.setFailureReason(null);
        response.setPaymentUrl(paymentUrl);
        return response;
    }

    public String generateSignature(Map<String, String> params, String passphrase) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        StringBuilder query = new StringBuilder();
        for (String key : keys) {
            String value = params.get(key);
            if (value != null && !value.isBlank()) {
                query.append(key).append('=').append(value.trim()).append('&');
            }
        }
        query.append("passphrase=").append(passphrase);

        return sha256Hex(query.toString());
    }

    // Validates the signature of the incoming PayFast webhook payload
    public boolean isSignatureValid(Map<String, Object> payload) {
        
        String receivedSignature = String.valueOf(payload.getOrDefault("signature", ""));
        
        if (receivedSignature.isBlank()) {
            return false;
        }

        Map<String, String> params = new LinkedHashMap<>();
        payload.forEach((key, value) -> {

            if (!"signature".equalsIgnoreCase(key) && value != null) {
                params.put(key, String.valueOf(value));
            }
        });

        String expectedSignature = generateSignature(params, payFastProperties.getPassphrase());
        return receivedSignature.equalsIgnoreCase(expectedSignature);
    }

    // Helper method to compute SHA-256 hash and return it as a hexadecimal string
  
    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Unable to compute PayFast signature", ex);
        }
    }
}
