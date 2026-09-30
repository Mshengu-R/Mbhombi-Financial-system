package com.banking.accountservice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import com.banking.accountservice.dto.AccountResponse;
import com.banking.accountservice.dto.CreateAccountRequest;
import com.banking.accountservice.entity.Account;
import com.banking.accountservice.repository.AccountRepository;
import com.banking.accountservice.service.AccountService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

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
	void accountOwnershipComesFromAuthenticatedCustomerId() throws Throwable {
		CreateAccountRequest request = new CreateAccountRequest();
		request.setAccountHolderName("Test Customer");
		request.setPhone("+27123456789");
		request.setAccountType("SAVINGS");
		request.setPin(new BigDecimal("1234"));
		request.setInitialDeposit(new BigDecimal("50.00"));

		AccountResponse response = accountService.createAccount("jwt-customer-123", request);
		Account savedAccount = accountRepository.findByAccountNumber(response.getAccountNumber())
				.orElseThrow(Assertions::fail);

		assertEquals("jwt-customer-123", savedAccount.getCustomerId());
	}

}
