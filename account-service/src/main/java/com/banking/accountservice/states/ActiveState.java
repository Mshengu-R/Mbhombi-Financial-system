package com.banking.accountservice.states;

import java.math.BigDecimal;

import com.banking.accountservice.entity.Account;
import com.banking.accountservice.entity.AccountStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
public final class ActiveState implements AccountState {

    static final ActiveState INSTANCE = new ActiveState();

    private ActiveState() {
    }

    @Override
    public void deposit(Account account, BigDecimal amount) {
        requirePositiveAmount(amount);
        account.setBalance(account.getBalance().add(amount));
    }

    @Override
    public void withdraw(Account account, BigDecimal amount) {
        requirePositiveAmount(amount);
        if (account.getBalance().compareTo(amount) < 0) {
            log.warn("Insufficient funds for account={} currentBalance={}",
                    account.getAccountNumber(), account.getBalance());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Insufficient funds for account " + account.getAccountNumber());
        }

        account.setBalance(account.getBalance().subtract(amount));
    }

    @Override
    public void block(Account account) {
        account.setStatus(AccountStatus.BLOCKED);
        log.info("Account: \"{}\", has  been blocked succesfully", account.getAccountNumber());
    }

    @Override
    public void unblock(Account account) {
    }

    @Override
    public void close(Account account) {
        account.setStatus(AccountStatus.CLOSED);
    }

    private void requirePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Amount must be greater than zero.");
        }
    }
}
