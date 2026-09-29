package com.banking.accountservice.service;

import com.banking.accountservice.dto.AccountResponse;
import com.banking.accountservice.dto.CreateAccountRequest;
import com.banking.accountservice.entity.Account;
import com.banking.accountservice.entity.AccountStatus;
import com.banking.accountservice.entity.AccountType;
import com.banking.accountservice.repository.AccountRepository;
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
public class AccountService {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String DEFAULT_BANK_NAME = "Mbhombi Bank";
    private static final BigDecimal SAVINGS_TRANSACTION_LIMIT = new BigDecimal("100000");
    private static final BigDecimal DEFAULT_TRANSACTION_LIMIT = new BigDecimal("500000");
    private static final String currency = "ZAR";

    private final AccountRepository accountRepository;

    /**
     * Create a new account from the incoming request.
     *
     * @param request request body payload with account information
     * @return created account details
     */
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        log.info("Creating account for customer={}", request.getCustomerId());

        // if (accountRepository.existsByAccountId(request.getPhone())) {
        //     log.warn("Account with email {} already exists", request.getEmail());
        //     throw new ResponseStatusException(HttpStatus.CONFLICT,
        //             "Account with email \"" + request.getEmail() + "\" already exists");
        // }

        AccountType accountType = parseAccountType(request.getAccountType());
        Account account = buildAccount(request, accountType);

        Account savedAccount = accountRepository.save(account);
        log.info("Account created accountNumber={}", savedAccount.getAccountNumber());
        return mapToResponse(savedAccount);
    }

    private AccountType parseAccountType(String accountTypeName) {
        if (accountTypeName == null || accountTypeName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Account type is required.");
        }
        try {
            return AccountType.valueOf(accountTypeName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid account type {}", accountTypeName);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid account type: " + accountTypeName);
        }
    }

    // Builds an Account entity from the request and account type.
    private Account buildAccount(CreateAccountRequest request, AccountType accountType) {
        Account account = new Account();
        account.setAccountHolderName(request.getAccountHolderName());
        account.setPhone(request.getPhone());
        account.setCurrency(currency);
        account.setCustomerId(request.getCustomerId());
        account.setBankName(DEFAULT_BANK_NAME);
        account.setPin(request.getPin());
        account.setAccountType(accountType);
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(request.getInitialDeposit());
        account.setAccountNumber(generateAccountNumber());
        account.setDailyTransactionLimit(accountType == AccountType.SAVINGS
                ? SAVINGS_TRANSACTION_LIMIT
                : DEFAULT_TRANSACTION_LIMIT);
        return account;
    }

    private AccountResponse mapToResponse(Account account) {
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setAccountHolderName(account.getAccountHolderName());
        response.setBankName(account.getBankName());
        response.setAccountType(account.getAccountType());
        response.setStatus(account.getStatus());
        response.setBalance(account.getBalance());
        response.setDailyTransactionLimit(account.getDailyTransactionLimit());
        response.setCreatedAt(account.getCreatedAt());
        return response;
    }

    private Account findAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Account not found for account number: " + accountNumber));
    }

    public AccountResponse getAccount(String accountNumber) {
        return mapToResponse(findAccountByNumber(accountNumber));
    }

    public BigDecimal getBalance(String accountNumber) {
        return findAccountByNumber(accountNumber).getBalance();
    }

    @Transactional
    public void blockAccount(String accountNumber) {
        log.info("Blocking account={}", accountNumber);
        Account account = findAccountByNumber(accountNumber);
        account.setStatus(AccountStatus.BLOCKED);
        accountRepository.save(account);
    }

    @Transactional
    public void unblockAccount(String accountNumber) {
        log.info("Unblocking account={}", accountNumber);
        Account account = findAccountByNumber(accountNumber);
        account.setStatus(AccountStatus.ACTIVE);
        account.setUpdatedAt(account.getUpdatedAt());
        accountRepository.save(account);
    }

    @Transactional
    public void deductBalance(String accountNumber, BigDecimal amount) {
        log.info("Deducting amount {} from account={}", amount, accountNumber);
        Account account = findAccountByNumber(accountNumber);

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
        Account account = findAccountByNumber(accountNumber);
        validateAmount(amount, "Credit amount must be greater than zero.");

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        log.info("Credit successful. New balance for account {} is {}", accountNumber, account.getBalance());
    }

    private void validateAccountActive(String accountNumber, Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
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

    private void validateSufficientBalance(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            log.warn("Insufficient funds for account={} currentBalance={}", account.getAccountNumber(), account.getBalance());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Insufficient funds for account " + account.getAccountNumber());
        }
    }

    // Generates a unique 12-digit account number.
    private String generateAccountNumber() {
        String accountNumber;
        do {
            long number = Math.abs(secureRandom.nextLong() % 1000000000000L);
            accountNumber = String.format("%012d", number);
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }
}
