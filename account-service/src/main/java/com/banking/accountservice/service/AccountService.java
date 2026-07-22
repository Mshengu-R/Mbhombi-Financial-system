package com.banking.accountservice.service;

import com.banking.accountservice.repository.AccountRepository;
import com.banking.accountservice.dto.CreateAccountRequest;
import com.banking.accountservice.dto.AccountResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.security.SecureRandom;

import javax.management.RuntimeErrorException;

import org.springframework.stereotype.Service;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import com.banking.accountservice.entity.Account;
import com.banking.accountservice.entity.AccountType;
import com.banking.accountservice.entity.AccountStatus;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService {


    private final AccountRepository accountRepository;
    private static SecureRandom secureRandom = new SecureRandom();



    public AccountResponse createAccount(CreateAccountRequest request) {
        log.info("Creating account for: {}", request.getEmail());

        // if an account already exists
        if (accountRepository.existsByEmail(request.getEmail())) {
            log.warn("Account with email {} already exists", request.getEmail());
            throw new RuntimeException("Account with this email \"" + request.getEmail() + "\" already exists");
        }

        Account account = new Account();
        account.setAccountHolderName(request.getAccountHolderName());
        account.setEmail(request.getEmail());
        account.setPhone(request.getPhone());
        // initial value of account type was:
        // account.setAccountType(request.getAccountType())
        account.setAccountType(AccountType.valueOf(request.getAccountType().toUpperCase()));
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(request.getInitialDeposit());
        account.setAccountNumber(generateAccountNumber());
        account.setDailyTransactionLimit(
            request.getAccountType().equals(AccountType.SAVINGS) ? new BigDecimal("100000") : new BigDecimal("500000")
        );


        // Save the account to the database
        Account savedAccount = accountRepository.save(account);
        log.info("Account created {}", savedAccount.getAccountNumber());

        return mapToResponse(savedAccount);
        // return null;
    }

 // this is for mapping account entity to response DTO
    private AccountResponse mapToResponse(Account account){
        AccountResponse response = new AccountResponse();
            response.setAccountNumber(account.getAccountNumber());
            response.setAccountHolderName(account.getAccountHolderName());
            response.setEmail(account.getEmail());
            response.setPhone(account.getPhone());
            response.setAccountType(account.getAccountType());
            response.setStatus(account.getStatus());
            response.setBalance(account.getBalance());
            response.setDailyTransactionLimit(account.getDailyTransactionLimit());
            
            return response;
    }

// Get account by account number @params -> accountNumber
public AccountResponse getAccount(String accountNumber){

    Account account = accountRepository.findByAccountNumber(accountNumber)
                    .orElseThrow(() -> new RuntimeException("Account not found"));

                    return mapToResponse(account);
                }


// Get account balance by account number @params -> accountNumber
public BigDecimal getBalance(String accountNumber){

    Account account = accountRepository.findByAccountNumber(accountNumber)
                    .orElseThrow(() -> new RuntimeException("Account not found"));

                    return account.getBalance();
                }


// Block account
public void blockAccount(String accountNumber) {
    log.info("Blocking account: {}", accountNumber);
    Account account = accountRepository.findByAccountNumber(accountNumber)
                    .orElseThrow(() -> new RuntimeException("Account not found"));

                    account.setStatus(AccountStatus.BLOCKED);
                    log.info("Account blocked: {}", accountNumber);
}


// for ublocking
public void unblockAccount(String accountNumber) {
    log.info("Unblocking account: {}", accountNumber);
    Account account = accountRepository.findByAccountNumber(accountNumber)
                    .orElseThrow(() -> new RuntimeException("Account not found"));

                    account.setStatus(AccountStatus.ACTIVE);
}

// Deduct balance from the sender
// 
public void deductBalance(String accountNumber, BigDecimal amount) {
    log.info("Deducting balance {} from account {} ", accountNumber);
     Account account = accountRepository.findByAccountNumber(accountNumber)
                    .orElseThrow(() -> new RuntimeException("Account not found"));

                    if(account.getStatus() != AccountStatus.ACTIVE) {
                        throw new RuntimeException("Account \""+ accountNumber + "\" is active or doesn't exist. If this is unusual please contact our helpline");


                    }

                    if(account.getBalance().compareTo(amount) <= 0) {
                        throw new RuntimeException("Insufficient funds for account "+ accountNumber);

                    }

                    account.setBalance(account.getBalance().subtract(amount));
                    accountRepository.save(account);
                    log.info("Balance updated. New Balance {}", account.getBalance());
}


// credit balance
// params accountnumber, amount
public void creditBalance(String accountNumber, BigDecimal amount) {

}


    // for generating an account number
    private String generateAccountNumber() {
        String accountNumber;

        do{
            long number = secureRandom.nextLong(1_000_000_000_000L);
            
            accountNumber = String.format("%012d", number);

        }while(accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
        
            // return java.util.UUID.randomUUID().toString();
    }
}