package com.banking.communityaccountservice.service;

import com.banking.communityaccountservice.dto.CommunityAccountResponse;
import com.banking.communityaccountservice.dto.CommunityCreateAccountRequest;
import com.banking.communityaccountservice.entity.CommunityAccount;
import com.banking.communityaccountservice.entity.CommunityAccountStatus;
// import com.banking.communityaccountservice.entity.CommunityAccountType;
import com.banking.communityaccountservice.repository.CommunityAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CommunityAccountServiceTest {

    @Mock
    private CommunityAccountRepository accountRepository;

    @InjectMocks
    private CommunityAccountService communityAccountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAccountShouldCreatePrivateCommunityWithJoinCode() {
        CommunityCreateAccountRequest request = new CommunityCreateAccountRequest();
        // request.setAccountHolderName("Lerato");
        request.setCreatorName("Lerato");
        request.setName("Soweto Helpers");
        request.setDescription("A group for saving together");
        request.setRulesDescription("Members can claim funds once per month");
        request.setAccountType("STOKVEL");
        request.setPrivateCommunity(true);
        request.setInitialDeposit(new BigDecimal("100"));

        when(accountRepository.existsByAccountNumber(any(String.class))).thenReturn(false);
        when(accountRepository.save(any(CommunityAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommunityAccountResponse response = communityAccountService.createAccount(request);

        assertNotNull(response.getAccountNumber());
        assertNotNull(response.getJoinCode());
        assertTrue(response.isPrivateCommunity());
        assertTrue(response.getMembers().contains("Lerato"));
    }

    @Test
    void joinCommunityShouldRequireJoinCodeForPrivateCommunity() {
        CommunityAccount account = new CommunityAccount();
        account.setAccountNumber("123456789012");
        account.setPrivateCommunity(true);
        account.setJoinCode("7654321");
        account.setStatus(CommunityAccountStatus.ACTIVE);
        account.setBalance(new BigDecimal("500"));
        account.setMembers(Set.of("Lerato"));

        when(accountRepository.findByAccountNumber("123456789012")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(CommunityAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommunityAccountResponse response = communityAccountService.joinCommunity("123456789012", "Thabo", "7654321");

        assertTrue(response.getMembers().contains("Thabo"));
        assertEquals(2, response.getMembers().size());
    }
}
