package com.banking.communityaccountservice.service;

import com.banking.communityaccountservice.dto.CommunityAccountResponse;
import com.banking.communityaccountservice.dto.CommunityCreateAccountRequest;
import com.banking.communityaccountservice.dto.CommunityClaimResponse;
import com.banking.communityaccountservice.dto.CommunityDescriptionUpdateRequest;
import com.banking.communityaccountservice.entity.CommunityAccount;
import com.banking.communityaccountservice.entity.CommunityAccountStatus;
import com.banking.communityaccountservice.entity.CommunityAccountType;
import com.banking.communityaccountservice.entity.CommunityMemberRole;
import com.banking.communityaccountservice.entity.CommunityMembership;
import com.banking.communityaccountservice.entity.CommunityMembershipStatus;
import com.banking.communityaccountservice.repository.CommunityAccountRepository;
import com.banking.communityaccountservice.repository.CommunityMembershipRepository;
import org.springframework.kafka.core.KafkaTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Service containing business operations for community accounts.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CommunityAccountService {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String DEFAULT_BANK_NAME = "Mbhombi Bank";
    private static final BigDecimal SAVINGS_TRANSACTION_LIMIT = new BigDecimal("100000");
    private static final BigDecimal DEFAULT_TRANSACTION_LIMIT = new BigDecimal("500000");
    private static final String COMMUNITY_CLAIMED_TOPIC = "community.funds.claimed";
    private static final boolean PrivateAcc = true;

    private final CommunityAccountRepository accountRepository;
    private final CommunityMembershipRepository membershipRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Create a new community account from the incoming request.
     */
    @Transactional
    public CommunityAccountResponse createAccount(String creatorCustomerId, CommunityCreateAccountRequest request) {
        validateRequired(creatorCustomerId, "Creator customer id");
        CommunityAccountType accountType = parseAccountType(request.getAccountType());
        CommunityAccount account = buildAccount(request, accountType);
        account.setCustomerId(creatorCustomerId.trim());

        CommunityAccount savedAccount = accountRepository.save(account);
        createCreatorMembership(savedAccount, request);
        log.info("Community account created accountNumber={}", savedAccount.getAccountNumber());
        return mapToResponse(savedAccount);
    }


    
 private CommunityAccountType parseAccountType(String accountTypeName) {
      
        if (accountTypeName == null || accountTypeName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account type is required.");
        }

        try {
            return CommunityAccountType.valueOf(accountTypeName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid account type {}", accountTypeName);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid account type: " + accountTypeName);
        }
    }


    private CommunityAccount buildAccount(CommunityCreateAccountRequest request,
                                          CommunityAccountType accountType) {

        CommunityAccount account = new CommunityAccount();
        account.setName(request.getName());
        account.setCreatorName(request.getCreatorName());
        account.setDescription(request.getDescription());
        account.setRulesDescription(request.getRulesDescription());
        account.setBankName(DEFAULT_BANK_NAME);
        account.setAccountType(accountType);
        account.setOtherType(accountType == CommunityAccountType.OTHER ? request.getOtherType() : null);
        // account.setPrivateCommunity(request.isPrivateCommunity());
        account.setJoinCode(normalizeJoinCode(request.getJoinCode()));
        account.setStatus(CommunityAccountStatus.ACTIVE);
        account.setBalance(request.getInitialDeposit());
        account.setAccountNumber(generateAccountNumber());
    
        account.setDailyTransactionLimit(accountType == CommunityAccountType.STOKVEL
                ? SAVINGS_TRANSACTION_LIMIT
                : DEFAULT_TRANSACTION_LIMIT);
        return account;
    }


    // Map to response for the Community account using the response DTO
    private CommunityAccountResponse mapToResponse(CommunityAccount account) {
        CommunityAccountResponse response = new CommunityAccountResponse();
        response.setId(account.getId());
        response.setCreatorName(account.getCreatorName());
        response.setName(account.getName());
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

    // Central account repository for searching the account by its number
    private CommunityAccount findAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Account not found for account number: " + accountNumber));
    }


    public CommunityAccountResponse getAccount(String accountNumber) {
        return mapToResponse(findAccountByNumber(accountNumber));
    }

    // Method for other methods which requires getting the balance for the user
    public BigDecimal getBalance(String accountNumber) {
        return findAccountByNumber(accountNumber).getBalance();
    }

    @Transactional
    public void blockAccount(String accountNumber) {
        log.info("Blocking account={}", accountNumber);
        CommunityAccount account = findAccountByNumber(accountNumber);
        account.setStatus(CommunityAccountStatus.BLOCKED);
        accountRepository.save(account);
    }

    @Transactional
    public void unblockAccount(String accountNumber) {
        log.info("Unblocking account={}", accountNumber);
        CommunityAccount account = findAccountByNumber(accountNumber);
        account.setStatus(CommunityAccountStatus.ACTIVE);
        accountRepository.save(account);
    }

    @Transactional
    public void deductBalance(String accountNumber, BigDecimal amount) {
        log.info("Deducting amount {} from account={}", amount, accountNumber);
        CommunityAccount account = findAccountByNumber(accountNumber);

        validateAccountActive(accountNumber, account);
        validateAmount(amount, "Deduction amount must be greater than zero.");
        validateSufficientBalance(account, amount);

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
        log.info("Deduction successful. New balance for account {} is {}", accountNumber, account.getBalance());
    }

    @Transactional
    public void creditBalance(String accountNumber, BigDecimal amount) {
        log.info("Crediting amount {} to account={}", amount, accountNumber);
        CommunityAccount account = findAccountByNumber(accountNumber);
        validateAmount(amount, "Credit amount must be greater than zero.");

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        log.info("Credit successful. New balance for account {} is {}", accountNumber, account.getBalance());
    }

    /**
     * Allow a member of the community to claim funds from the pooled account.
     */
    @Transactional
    public CommunityClaimResponse claimFunds(String accountNumber, String customerId, BigDecimal amount) {
        CommunityAccount account = findAccountByNumber(accountNumber);
        validateAccountActive(accountNumber, account);
        validateAmount(amount, "Claim amount must be greater than zero.");
        validateRequired(customerId, "Customer id");
     
        CommunityMembership membership = membershipRepository
                .findByCommunityIdAndCustomerId(account.getId(), customerId.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Customer is not a member of this community."));
       
                        if (membership.getStatus() != CommunityMembershipStatus.JOINED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customer is not an active member of this community.");
        }

        validateSufficientBalance(account, amount);
        account.setBalance(account.getBalance().subtract(amount));
        CommunityAccount savedAccount = accountRepository.save(account);
        publishClaimNotification(savedAccount, customerId.trim(), amount);
        return new CommunityClaimResponse(accountNumber, customerId.trim(), amount, savedAccount.getBalance());
    }

    // ADDED: creator-only description update, identified by the stored creator customer ID.
    @Transactional
    public CommunityAccountResponse updateDescription(String accountNumber, String customerId,
                                                      CommunityDescriptionUpdateRequest request) {
        CommunityAccount account = findAccountByNumber(accountNumber);
        validateRequired(customerId, "Customer id");
        if (!account.getCustomerId().equals(customerId.trim())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the community creator can update its description.");
        }
        account.setDescription(request.getDescription().trim());
        if (request.getRulesDescription() != null && !request.getRulesDescription().isBlank()) {
            account.setRulesDescription(request.getRulesDescription().trim());
        }
        return mapToResponse(accountRepository.save(account));
    }

  /*
   * THE CODE BELOW
   * - WHEN A CUSTOMER CREATES A COMMUNITY ACCOUNT
   *  THE ACCOUNT SHOULD DEFUALT TO THE USER BEING THE ADMIN 
   */  
    private void createCreatorMembership(CommunityAccount account, CommunityCreateAccountRequest request) {
        CommunityMembership membership = new CommunityMembership();
        membership.setCommunityId(account.getId());
        membership.setCustomerId(account.getCustomerId());
        // membership.setDisplayName(request.getDisplayName().trim());
        membership.setAccountNumber(account.getAccountNumber());
        membership.setBankName(account.getBankName());
        // CHANGED: ADMIN is the existing role representing the creator.
        membership.setRole(CommunityMemberRole.ADMIN);
        // membership.setPrivateCommunity(account.isPrivateCommunity());
        membership.setStatus(CommunityMembershipStatus.JOINED);
        membership.setDailyTransactionLimit(account.getDailyTransactionLimit());
        membershipRepository.save(membership);
    }


    
    public void lockAccount(String accountNumber) {

        log.info("locking account={}", accountNumber);
        CommunityAccount account = findAccountByNumber(accountNumber);
        account.setStatus(CommunityAccountStatus.LOCKED);
        accountRepository.save(account);

    }


    // Notifications for when users are claiming the money
    private void publishClaimNotification(CommunityAccount account, String claimantId, BigDecimal amount) {
        List<String> recipients = membershipRepository
                .findByCommunityIdAndStatus(account.getId(), CommunityMembershipStatus.JOINED)
                .stream()
                .map(CommunityMembership::getCustomerId)
                .filter(customerId -> !customerId.equals(claimantId))
                .toList();
        if (recipients.isEmpty()) {
            return;
        }
        Map<String, Object> event = new HashMap<>();
        event.put("communityId", account.getId());
        event.put("accountNumber", account.getAccountNumber());
        event.put("claimantId", claimantId);
        event.put("amount", amount);
        event.put("recipients", recipients);
        kafkaTemplate.send(COMMUNITY_CLAIMED_TOPIC, account.getAccountNumber(), event);
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

    private void validateAmount(BigDecimal amount, String errorMessage) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
        }
    }

    private void validateSufficientBalance(CommunityAccount account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            log.warn("Insufficient funds for account={} currentBalance={}", account.getAccountNumber(), account.getBalance());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Insufficient funds for account " + account.getAccountNumber());
        }
    }

    private String normalizeJoinCode(String joinCode) {
        if (joinCode == null || joinCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Private community join code is required.");
        }
        String normalized = joinCode.trim();
        if (normalized.length() != 6 || !normalized.matches("\\d+")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Join code must be exactly 6 digits.");
        }
        return normalized;
    }

    private String generateAccountNumber() {
        String communityAccountNumber;
        do {
            long number = Math.abs(secureRandom.nextLong() % 1000000000000L);
            communityAccountNumber = String.format("%012d", number);
        } while (accountRepository.existsByAccountNumber(communityAccountNumber));
        return communityAccountNumber;
    }
}
