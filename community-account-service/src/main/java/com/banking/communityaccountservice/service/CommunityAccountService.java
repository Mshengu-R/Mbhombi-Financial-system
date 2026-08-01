package com.banking.communityaccountservice.service;

import com.banking.communityaccountservice.dto.CommunityAccountResponse;
import com.banking.communityaccountservice.dto.CommunityCreateAccountRequest;
import com.banking.communityaccountservice.entity.CommunityAccount;
import com.banking.communityaccountservice.entity.CommunityAccountStatus;
import com.banking.communityaccountservice.entity.CommunityAccountType;
import com.banking.communityaccountservice.repository.CommunityAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.SecureRandom;

/**
 * Service containing business operations for account management.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CommunityAccountService {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String DEFAULT_BANK_NAME = "Mbhombi Bank";
    private static final BigDecimal SAVINGS_TRANSACTION_LIMIT = new BigDecimal("100000");
    private static final BigDecimal DEFAULT_TRANSACTION_LIMIT = new BigDecimal("500000");

    private final CommunityAccountRepository accountRepository;

    /**
     * Create a new account from the incoming request.
     *
     * @param request request body payload with account information
     * @return created account details
     */
    @Transactional
    public CommunityAccountResponse createAccount(CommunityCreateAccountRequest request) {
        // log.info("Creating account for email={}", request.getEmail());

        // if (accountRepository.existsByEmail(request.getEmail())) {
        //     log.warn("Account with email {} already exists", request.getEmail());
        //     throw new ResponseStatusException(HttpStatus.CONFLICT,
        //             "Account with email \"" + request.getEmail() + "\" already exists");
        // }

        CommunityAccountType accountType = parseAccountType(request.getAccountType());
        CommunityAccount account = buildAccount(request, accountType);

        CommunityAccount savedAccount = accountRepository.save(account);
        log.info("Account created accountNumber={}", savedAccount.getAccountNumber());
        return mapToResponse(savedAccount);
    }

    private CommunityAccountType parseAccountType(String accountTypeName) {
        if (accountTypeName == null || accountTypeName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Account type is required.");
        }
        try {
            return CommunityAccountType.valueOf(accountTypeName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid account type {}", accountTypeName);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid account type: " + accountTypeName);
        }
    }

    // Builds an Account entity from the request and account type.
    private CommunityAccount buildAccount(CommunityCreateAccountRequest request, CommunityAccountType accountType) {
        CommunityAccount account = new CommunityAccount();
        account.setAccountHolderName(request.getAccountHolderName());
        // account.setEmail(request.getEmail());
        // account.setPhone(request.getPhone());
        account.setBankName(DEFAULT_BANK_NAME);
        account.setAccountType(accountType);
        account.setOtherType(accountType == CommunityAccountType.OTHER ? request.getOtherType() : null);
        account.setStatus(CommunityAccountStatus.ACTIVE);
        account.setBalance(request.getInitialDeposit());
        account.setAccountNumber(generateAccountNumber());
        account.setDailyTransactionLimit(accountType == CommunityAccountType.STOKVEL
                ? SAVINGS_TRANSACTION_LIMIT
                : DEFAULT_TRANSACTION_LIMIT);
        return account;
    }

    private CommunityAccountResponse mapToResponse(CommunityAccount account) {
        CommunityAccountResponse response = new CommunityAccountResponse();
        response.setId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setAccountHolderName(account.getAccountHolderName());
        // response.setEmail(account.getEmail());
        // response.setPhone(account.getPhone());
        response.setBankName(account.getBankName());
        response.setAccountType(account.getAccountType());
        response.setStatus(account.getStatus());
        response.setBalance(account.getBalance());
        response.setDailyTransactionLimit(account.getDailyTransactionLimit());
        response.setCreatedAt(account.getCreatedAt());
        return response;
    }

    private CommunityAccount findAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Account not found for account number: " + accountNumber));
    }

    public CommunityAccountResponse getAccount(String accountNumber) {
        return mapToResponse(findAccountByNumber(accountNumber));
    }

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

    private String generateAccountNumber() {
        String accountNumber;
        do {
            long number = Math.abs(secureRandom.nextLong() % 1000000000000L);
            accountNumber = String.format("%012d", number);
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }
}
