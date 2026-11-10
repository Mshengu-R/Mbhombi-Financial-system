package com.banking.accountservice.states;

import java.math.BigDecimal;

import com.banking.accountservice.entity.Account;
import com.banking.accountservice.entity.AccountStatus;

// Abstract
public interface AccountState {

    void deposit(Account account, BigDecimal amount);

    void withdraw(Account account, BigDecimal amount);

    void block(Account account);

    void unblock(Account account);

    void close(Account account);

    static AccountState from(AccountStatus status) {
        if (status == null) {
            throw new IllegalStateException("Account status is required");
        }

        return switch (status) {
            case ACTIVE -> ActiveState.INSTANCE;
            case BLOCKED -> BlockedState.INSTANCE;
            case CLOSED -> ClosedState.INSTANCE;
        };
    }
}
