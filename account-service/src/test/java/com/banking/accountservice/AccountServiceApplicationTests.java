package com.banking.accountservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import com.banking.accountservice.dto.AccountResponse;
import com.banking.accountservice.dto.CreateAccountRequest;
import com.banking.accountservice.entity.Account;
import com.banking.accountservice.entity.AccountStatus;
import com.banking.accountservice.repository.AccountRepository;
import com.banking.accountservice.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@ActiveProfiles("test")
class AccountServiceApplicationTests {
	@Autowired
	private AccountService accountService;

	@Autowired
	private AccountRepository accountRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void accountOwnershipComesFromAuthenticatedCustomerId() {
		CreateAccountRequest request = new CreateAccountRequest();
		request.setAccountHolderName("Test Customer");
		request.setPhone("+27123456789");
		request.setAccountType("SAVINGS");
		request.setPin(new BigDecimal("1234"));
		request.setInitialDeposit(new BigDecimal("50.00"));

		AccountResponse response = accountService.createAccount("jwt-customer-123", request);
		Account savedAccount = accountRepository.findByAccountNumber(response.getAccountNumber())
				.orElseThrow(() -> new IllegalStateException("Account was not saved"));

		assertEquals("jwt-customer-123", savedAccount.getCustomerId());
	}

	@Test
	void accountServiceUsesStateForCreditDebitAndTransitions() {
		CreateAccountRequest request = new CreateAccountRequest();
		request.setAccountHolderName("State Pattern Customer");
		request.setPhone("+27123456789");
		request.setAccountType("SAVINGS");
		request.setPin(new BigDecimal("1234"));
		request.setInitialDeposit(new BigDecimal("50.00"));

		AccountResponse response = accountService.createAccount("state-customer", request);
		String accountNumber = response.getAccountNumber();

		accountService.blockAccount(accountNumber);
		accountService.creditBalance(accountNumber, new BigDecimal("10.00"));

		Account blockedAccount = accountRepository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new IllegalStateException("Account was not found after block"));
		assertEquals(AccountStatus.BLOCKED, blockedAccount.getStatus());
		assertEquals(0, blockedAccount.getBalance().compareTo(new BigDecimal("60.00")));
		assertThrows(ResponseStatusException.class,
				() -> accountService.deductBalance(accountNumber, BigDecimal.ONE));

		accountService.unblockAccount(accountNumber);
		accountService.deductBalance(accountNumber, new BigDecimal("10.00"));

		Account activeAccount = accountRepository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new IllegalStateException("Account was not found after unblock"));
		assertEquals(AccountStatus.ACTIVE, activeAccount.getStatus());
		assertEquals(0, activeAccount.getBalance().compareTo(new BigDecimal("50.00")));
	}

}
