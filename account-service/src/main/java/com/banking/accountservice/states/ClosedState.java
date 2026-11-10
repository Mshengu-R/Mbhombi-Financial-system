package com.banking.accountservice.states;

import java.math.BigDecimal;

import com.banking.accountservice.entity.Account;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
public final class ClosedState implements AccountState {

    static final ClosedState INSTANCE = new ClosedState();

    private ClosedState() {
    }

    @Override
    public void deposit(Account account, BigDecimal amount) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Cannot deposit into a closed account");
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
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Closed account cannot be frozen");
    }

    @Override
    public void unblock(Account account) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Closed account cannot be unblocked");
    }

    @Override
    public void close(Account account) {
    }
}
