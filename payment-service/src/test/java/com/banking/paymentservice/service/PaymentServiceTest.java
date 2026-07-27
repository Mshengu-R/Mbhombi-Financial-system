package com.banking.paymentservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.banking.paymentservice.dto.CreatePaymentRequest;
import com.banking.paymentservice.dto.PaymentOrderResponse;
import com.banking.paymentservice.entity.Payment;
import com.banking.paymentservice.entity.PaymentStatus;
import com.banking.paymentservice.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PayFastService payFastService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void createPaymentOrderShouldPersistPaymentAndReturnResponse() {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setAccountNumber("ACC-100");
        request.setAmount(new BigDecimal("250.00"));
        request.setCurrency("ZAR");
        request.setDescription("School fees");

        Payment payment = new Payment();
        payment.setId("pay-123");
        payment.setAccountNumber(request.getAccountNumber());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setDescription(request.getDescription());
        payment.setStatus(PaymentStatus.CREATED);

        PaymentOrderResponse payFastResponse = new PaymentOrderResponse();
        payFastResponse.setPayFastOderId("payfast-1");
        payFastResponse.setPayFastId("payfast-1");
        payFastResponse.setCurrency("ZAR");
        payFastResponse.setDescription("School fees");
        payFastResponse.setPaymentUrl("https://sandbox.payfast.co.za/eng/process");

        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(payFastService.createPaymentOrder(any(CreatePaymentRequest.class))).thenReturn(payFastResponse);

        PaymentOrderResponse response = paymentService.createPaymentOrder(request);

        assertNotNull(response);
        assertEquals("pay-123", response.getId());
        assertEquals(request.getAccountNumber(), response.getAccountNumber());
        assertEquals(request.getAmount(), response.getAmount());
        verify(paymentRepository).save(any(Payment.class));
        verify(kafkaTemplate).send(any(), any(), any());
    }
}
