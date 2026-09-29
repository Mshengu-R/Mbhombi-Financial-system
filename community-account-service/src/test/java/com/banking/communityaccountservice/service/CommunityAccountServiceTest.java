// package com.banking.communityaccountservice.service;

// import com.banking.communityaccountservice.dto.CommunityAccountResponse;
// import com.banking.communityaccountservice.dto.CommunityCreateAccountRequest;
// import com.banking.communityaccountservice.entity.CommunityAccount;
// import com.banking.communityaccountservice.entity.CommunityAccountStatus;
// import com.banking.communityaccountservice.dto.JoinCommunityRequest;
// import com.banking.communityaccountservice.dto.JoinCommunityResponse;
// import com.banking.communityaccountservice.entity.CommunityAccount;
// import com.banking.communityaccountservice.repository.CommunityAccountRepository;
// import com.banking.communityaccountservice.repository.CommunityMembershipRepository;
// import org.springframework.kafka.core.KafkaTemplate;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;

// import java.math.BigDecimal;
// import java.util.UUID;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// class CommunityAccountServiceTest {

//     @Mock
//     private CommunityAccountRepository accountRepository;

//     @Mock
//     private CommunityMembershipRepository membershipRepository;

//     @Mock
//     private KafkaTemplate<String, Object> kafkaTemplate;

//     @InjectMocks
//     private CommunityAccountService communityAccountService;

//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);
//     }

//     @Test
//     void createAccountShouldCreatePrivateCommunityWithJoinCode() {
//         CommunityCreateAccountRequest request = new CommunityCreateAccountRequest();
//         // request.setAccountHolderName("Lerato");
//         request.setCreatorName("Lerato");
//         request.setName("Soweto Helpers");
//         request.setDescription("A group for saving together");
//         request.setRulesDescription("Members can claim funds once per month");
//         request.setAccountType("STOKVEL");
//         request.setCustomerId("customer-123");
//         request.setJoinCode("765432");
//         request.setInitialDeposit(new BigDecimal("100"));

//         when(accountRepository.existsByAccountNumber(any(String.class))).thenReturn(false);
//         when(accountRepository.save(any(CommunityAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

//         CommunityAccountResponse response = communityAccountService.createAccount("customer-123", request);

//         assertNotNull(response.getAccountNumber());
//         assertEquals("765432", response.getJoinCode());
//         verify(membershipRepository).save(any());
//     }

//     @Test
//     void joinCommunityShouldRequireJoinCodeForPrivateCommunity() {
//         CommunityAccount account = new CommunityAccount();
//         account.setId("community-123");
//         account.setAccountNumber("123456789012");
//         account.setJoinCode("765432");
//         account.setStatus(CommunityAccountStatus.ACTIVE);

//         // JoinCommunityRequest request = new JoinCommunityRequest(
//         //     "community-123", "Thabo", "Thabo", "123456789012",
//             // "Mbhombi Bank", "MEMBER", null, null);

//         when(accountRepository.findByAccountNumber("123456789012")).thenReturn(java.util.Optional.of(account));
//         when(membershipRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

//         // CommunityMembershipService membershipService = new CommunityMembershipService(
//         //     accountRepository, membershipRepository);
//         // JoinCommunityResponse response = membershipService.joinCommunity(
//             // "123456789012", "customer-456", "765432", request);

//         assertEquals("customer-456", response.getCustomerId());
//         assertEquals("JOINED", response.getStatus());
//     }
// }
