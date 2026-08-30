package com.banking.authenticationservice.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banking.authenticationservice.entity.Consent;
import com.banking.authenticationservice.entity.ConsentType;

public interface ConsentRepository extends JpaRepository<Consent, UUID> {
    List<Consent> findByCustomerId(UUID customerId);
    boolean existsByCustomerIdAndConsentTypeAndDocumentVersion(
            UUID customerId, ConsentType consentType, String documentVersion);
}