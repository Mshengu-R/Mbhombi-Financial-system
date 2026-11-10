package com.banking.accountservice.states;

import java.math.BigDecimal;

import com.banking.accountservice.entity.Account;
import com.banking.accountservice.entity.AccountStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
public final class BlockedState implements AccountState {

    static final BlockedState INSTANCE = new BlockedState();

    private BlockedState() {
    }

    @Override
    public void deposit(Account account, BigDecimal amount) {
        requirePositiveAmount(amount);
        account.setBalance(account.getBalance().add(amount));
    }

    @Override
    public void withdraw(Account account, BigDecimal amount) {
        log.warn("Attempted operation on non-active account={}", account.getAccountNumber());
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Account \"" + account.getAccountNumber()
                        + "\" is not active. Contact support if this is unexpected.");
    }

    @Override
    public void block(Account account) {
    }

    @Override
    public void unblock(Account account) {
        account.setStatus(AccountStatus.ACTIVE);
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
