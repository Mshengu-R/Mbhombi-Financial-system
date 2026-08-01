package com.banking.paymentservice.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banking.paymentservice.dto.CreatePaymentRequest;
import com.banking.paymentservice.dto.PaymentOrderResponse;
import com.banking.paymentservice.entity.Payment;
import com.banking.paymentservice.entity.PaymentStatus;
import com.banking.paymentservice.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PayFastService payFastService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String PAYMENT_CREATED_TOPIC = "payment.created";
    private static final String PAYMENT_COMPLETED_TOPIC = "payment.completed";
    private static final String PAYMENT_FAILED_TOPIC = "payment.failed";

    @Transactional
    public PaymentOrderResponse createPaymentOrder(CreatePaymentRequest request) {
        Payment payment = new Payment();
        payment.setAccountNumber(request.getAccountNumber());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency() == null || request.getCurrency().isBlank() ? "ZAR" : request.getCurrency());
        payment.setDescription(request.getDescription());
        payment.setStatus(PaymentStatus.PENDING);

        Payment savedPayment = paymentRepository.save(payment);

        PaymentOrderResponse response = payFastService.createPaymentOrder(request);
        response.setId(savedPayment.getId());
        response.setStatus(savedPayment.getStatus().name());
        response.setPaymentUrl(response.getPaymentUrl());

        savedPayment.setPayFastOderId(response.getPayFastOderId());
        savedPayment.setPayFastId(response.getPayFastId());
        savedPayment.setCurrency(response.getCurrency());
        savedPayment.setDescription(response.getDescription());
        paymentRepository.save(savedPayment);

        Map<String, Object> event = new HashMap<>();
        event.put("paymentId", savedPayment.getId());
        event.put("accountNumber", savedPayment.getAccountNumber());
        event.put("amount", savedPayment.getAmount());
        event.put("currency", savedPayment.getCurrency());
        event.put("status", savedPayment.getStatus().name());
        kafkaTemplate.send(PAYMENT_CREATED_TOPIC, savedPayment.getId(), event);

        log.info("PayFast order created for payment {}", savedPayment.getId());
        return response;
    }

    @Transactional
    public void handleWebhook(Map<String, Object> payload) {
        
        String paymentId = String.valueOf(payload.getOrDefault("m_payment_id", payload.getOrDefault("payment_id", "")));
       
        if (paymentId.isBlank()) {
            throw new IllegalArgumentException("Missing payment identifier in PayFast webhook");
        }

        if (!payFastService.isSignatureValid(payload)) {
            throw new IllegalArgumentException("Invalid PayFast signature");
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        String paymentStatus = String.valueOf(payload.getOrDefault("payment_status", payload.getOrDefault("status", ""))).toUpperCase();
        String payFastId = String.valueOf(payload.getOrDefault("pf_payment_id", ""));
        String reason = String.valueOf(payload.getOrDefault("custom_str1", payload.getOrDefault("reason", "")));

        if ("COMPLETE".equals(paymentStatus) || "SUCCESS".equals(paymentStatus)) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setPayFastId(payFastId.isBlank() ? payment.getPayFastId() : payFastId);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            Map<String, Object> event = new HashMap<>();
            event.put("paymentId", payment.getId());
            event.put("accountNumber", payment.getAccountNumber());
            event.put("amount", payment.getAmount());
            event.put("currency", payment.getCurrency());
            event.put("status", payment.getStatus().name());
            kafkaTemplate.send(PAYMENT_COMPLETED_TOPIC, payment.getId(), event);
            log.info("Payment completed and published: {}", payment.getId());
            return;
        }

        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(reason.isBlank() ? "Payment rejected by PayFast" : reason);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        Map<String, Object> event = new HashMap<>();
        event.put("paymentId", payment.getId());
        event.put("accountNumber", payment.getAccountNumber());
        event.put("amount", payment.getAmount());
        event.put("currency", payment.getCurrency());
        event.put("status", payment.getStatus().name());
        event.put("reason", payment.getFailureReason());
        kafkaTemplate.send(PAYMENT_FAILED_TOPIC, payment.getId(), event);
        log.warn("Payment failed and published: {}", payment.getId());
    }
}
