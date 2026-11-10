package com.banking.accountservice.states;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import com.banking.accountservice.entity.Account;
import com.banking.accountservice.entity.AccountStatus;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class AccountStateTest {

    @Test
    void activeStateAppliesExactDecimalChangesAndCanTransition() {
        Account account = account(AccountStatus.ACTIVE, "100.00");

        account.getState().deposit(account, new BigDecimal("25.50"));
        account.getState().withdraw(account, new BigDecimal("10.25"));
        account.getState().block(account);

        assertEquals(new BigDecimal("115.25"), account.getBalance());
        assertEquals(AccountStatus.BLOCKED, account.getStatus());
        assertEquals(BlockedState.INSTANCE, account.getState());
    }

    @Test
    void activeStateRejectsWithdrawalAboveAvailableBalance() {
        Account account = account(AccountStatus.ACTIVE, "10.00");

        assertThrows(ResponseStatusException.class,
                () -> account.getState().withdraw(account, new BigDecimal("10.01")));

        assertEquals(new BigDecimal("10.00"), account.getBalance());
    }

    @Test
    void blockedStateAllowsCreditsButRequiresUnblockingBeforeWithdrawal() {
        Account account = account(AccountStatus.BLOCKED, "20.00");

        account.getState().deposit(account, new BigDecimal("5.00"));
        assertThrows(ResponseStatusException.class,
                () -> account.getState().withdraw(account, BigDecimal.ONE));
        account.getState().unblock(account);

        assertEquals(new BigDecimal("25.00"), account.getBalance());
        assertEquals(AccountStatus.ACTIVE, account.getStatus());
    }

    @Test
    void closedStateRejectsFinancialOperationsAndCannotBeReactivated() {
        Account account = account(AccountStatus.CLOSED, "20.00");

        assertThrows(ResponseStatusException.class,
                () -> account.getState().deposit(account, BigDecimal.ONE));
        assertThrows(ResponseStatusException.class,
                () -> account.getState().unblock(account));

        assertEquals(new BigDecimal("20.00"), account.getBalance());
        assertEquals(AccountStatus.CLOSED, account.getStatus());
    }

    @Test
    void closingAnAccountChangesTheStateResolvedFromPersistedStatus() {
        Account account = account(AccountStatus.ACTIVE, "20.00");

        account.getState().close(account);

        assertEquals(AccountStatus.CLOSED, account.getStatus());
        assertEquals(ClosedState.INSTANCE, account.getState());
    }

    private Account account(AccountStatus status, String balance) {
        Account account = new Account();
        account.setAccountNumber("123456789012");
        account.setStatus(status);
        account.setBalance(new BigDecimal(balance));
        return account;
    }
}
