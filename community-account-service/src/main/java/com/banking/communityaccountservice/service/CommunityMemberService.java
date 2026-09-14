package com.banking.communityaccountservice.service;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.banking.communityaccountservice.dto.CommunityAccountResponse;
import com.banking.communityaccountservice.dto.JoinCommunityRequest;
import com.banking.communityaccountservice.dto.JoinCommunityResponse;
import com.banking.communityaccountservice.entity.CommunityAccount;
import com.banking.communityaccountservice.entity.CommunityAccountStatus;
import com.banking.communityaccountservice.entity.CommunityMemberRole;
import com.banking.communityaccountservice.entity.CommunityMembership;
import com.banking.communityaccountservice.entity.CommunityMembershipStatus;
import com.banking.communityaccountservice.repository.CommunityAccountRepository;
import com.banking.communityaccountservice.repository.CommunityMembershipRepository;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommunityMemberService {
    
    private final CommunityAccountRepository accountRepository;
    private final CommunityMembershipRepository membershipRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    /**
     * Creates a membership for the customer represented by the endpoint path.
     * The path values are authoritative; request values are checked to prevent cross-account joins.
     */
    @Transactional
    public JoinCommunityResponse joinCommunity(String accountNumber, String customerId,
                                               String joinCode, JoinCommunityRequest request) {
        CommunityAccount account = findAccountByNumber(accountNumber);
        validateAccountActive(accountNumber, account);
        validateRequired(accountNumber, "Account number");
        validateRequired(customerId, "Customer id");
        validateRequestMatchesAccount(account, accountNumber, customerId, request);

        if (joinCode == null || !joinCode.trim().equals(account.getJoinCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid join code for this community.");
        }

        if (request.getPin() == null || request.getPin().isBlank() || !request.getPin().trim().equals(account.getPin())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid community account pin.");
        }

        String communityId = request.getCommunityId();
        CommunityMembership existing = membershipRepository
                .findByCommunityIdAndCustomerId(communityId, customerId)
                .orElse(null);
        if (existing != null && existing.getStatus() == CommunityMembershipStatus.JOINED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer is already a member of this community.");
        }

        CommunityMembership membership = buildMembership(account, customerId, request, existing);
        CommunityMembership saved = membershipRepository.save(membership);
        publishCommunityActivity(account, "MEMBER_JOINED", customerId, "Member joined the community account", account.getBalance());
        return mapToResponse(saved);
    }

    // build up code for creating a membership
    private CommunityMembership buildMembership(CommunityAccount account, String customerId, 
                                                JoinCommunityRequest request, CommunityMembership existing) {
        CommunityMembership membership = existing == null ? new CommunityMembership() : existing;
        membership.setCommunityId(request.getCommunityId());
        membership.setCustomerId(customerId);
        membership.setDisplayName(request.getMemberName().trim());
        membership.setAccountNumber(request.getAccountNumber());
        membership.setBankName(request.getBankName());
        membership.setRole(parseRole(request.getRole()));
        // membership.setPrivateCommunity(account.isPrivateCommunity());
        membership.setStatus(CommunityMembershipStatus.JOINED);
        membership.setDailyTransactionLimit(account.getDailyTransactionLimit());
        return membership;
    }

    // Checking for user roles
    private CommunityMemberRole parseRole(String role) {
        if (role == null || role.isBlank()) {
            return CommunityMemberRole.MEMBER;
        }
        try {
            return CommunityMemberRole.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid community member role: " + role);
        }
    }

    private void validateRequestMatchesAccount(CommunityAccount account, String accountNumber, String customerId,
                                               JoinCommunityRequest request) {
        validateRequired(request.getCommunityId(), "Community id");
        validateRequired(customerId, "Customer id");
        // if (!customerId.equals(request.getCustomerId())) {
        //     throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer id does not match the requested member.");
        // }
        if (account.getId() != null && !account.getId().equals(request.getCommunityId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Community id does not match the account.");
        }
        if (!accountNumber.equals(request.getAccountNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account number does not match the account.");
        }
    }

    
    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required.");
        }
    }


       private void validateAccountActive(String accountNumber, CommunityAccount account) {
        if (account.getStatus() != CommunityAccountStatus.ACTIVE) {
            log.warn("Attempted operation on non-active account={}", accountNumber);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Account \"" + accountNumber + "\" is not active. Contact support if this is unexpected.");
        }
    }


     private CommunityAccount findAccountByNumber(String accountNumber) {
        // log.info("");
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Account not found for account number: " + accountNumber));
     }

     
     public CommunityAccountResponse getAccount(String accountNumber) {
        CommunityAccount account = findAccountByNumber(accountNumber);
        CommunityAccountResponse response = new CommunityAccountResponse();
        response.setId(account.getId());
        response.setName(account.getName());
        response.setCreatorName(account.getCreatorName());
        response.setDescription(account.getDescription());
        response.setRulesDescription(account.getRulesDescription());
        response.setAccountNumber(account.getAccountNumber());
        response.setBankName(account.getBankName());
        response.setOtherType(account.getOtherType());
        response.setAccountType(account.getAccountType());
        // response.setPrivateCommunity(account.isPrivateCommunity());
        response.setJoinCode(account.getJoinCode());
        response.setStatus(account.getStatus());
        response.setBalance(account.getBalance());
        response.setDailyTransactionLimit(account.getDailyTransactionLimit());
        response.setCreatedAt(account.getCreatedAt());
        return response;
     }

    /**
     * Marks an active membership as LEFT instead of deleting it, preserving the membership audit trail.
     */
    @Transactional
    public JoinCommunityResponse leaveCommunity(String accountNumber, String customerId) {
        CommunityAccount account = findAccountByNumber(accountNumber);
        validateAccountActive(accountNumber, account);
        validateRequired(customerId, "Customer id");

        CommunityMembership membership = membershipRepository
                .findByCommunityIdAndCustomerId(account.getId(), customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Membership not found for customer " + customerId));
        if (membership.getStatus() != CommunityMembershipStatus.JOINED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer is not an active member of this community.");
        }
        membership.setStatus(CommunityMembershipStatus.LEFT);
        CommunityMembership saved = membershipRepository.save(membership);
        publishCommunityActivity(account, "MEMBER_LEFT", customerId,
                "Member left the community account", account.getBalance());
        return mapToResponse(saved);
    }


    private JoinCommunityResponse mapToResponse(CommunityMembership membership) {
        JoinCommunityResponse response = new JoinCommunityResponse();
        response.setId(membership.getId());
        response.setCustomerId(membership.getCustomerId());
        response.setBankName(membership.getBankName());
        response.setDisplayName(membership.getDisplayName());
        response.setCommunityId(membership.getCommunityId());
        response.setAccountNumber(membership.getAccountNumber());
        response.setRole(membership.getRole().name());
        response.setStatus(membership.getStatus().name());
        response.setDailyTransactionLimit(membership.getDailyTransactionLimit());
        response.setCreatedAt(membership.getCreatedAt());
        return response;
    }

    private void publishCommunityActivity(CommunityAccount account, String eventType,
                                           String actorCustomerId, String message, BigDecimal balance) {
        java.util.Map<String, Object> event = new java.util.HashMap<>();
        event.put("eventType", eventType);
        event.put("communityId", account.getId());
        event.put("accountNumber", account.getAccountNumber());
        event.put("actorCustomerId", actorCustomerId);
        event.put("message", message);
        event.put("balance", balance);
        event.put("createdAt", java.time.LocalDateTime.now());
        kafkaTemplate.send("community.activity", account.getAccountNumber(), event);
        log.info("Published community membership activity topic=community.activity eventType={} accountNumber={}",
                eventType, account.getAccountNumber());
    }
}

