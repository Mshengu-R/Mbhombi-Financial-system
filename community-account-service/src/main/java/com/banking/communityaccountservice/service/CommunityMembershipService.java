package com.banking.communityaccountservice.service;

import com.banking.communityaccountservice.dto.JoinCommunityRequest;
import com.banking.communityaccountservice.dto.JoinCommunityResponse;
import com.banking.communityaccountservice.repository.CommunityAccountRepository;
import com.banking.communityaccountservice.repository.CommunityMembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommunityMembershipService {

    private final CommunityAccountRepository accountRepository;
    private final CommunityMembershipRepository membershipRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public JoinCommunityResponse joinCommunity(String accountNumber, String customerId,
                                               String joinCode, JoinCommunityRequest request) {
        CommunityMemberService memberService = new CommunityMemberService(accountRepository, membershipRepository, kafkaTemplate);
        return memberService.joinCommunity(accountNumber, customerId, joinCode, request);
    }

    @Transactional
    public JoinCommunityResponse leaveCommunity(String accountNumber, String customerId) {
        CommunityMemberService memberService = new CommunityMemberService(accountRepository, membershipRepository, kafkaTemplate);
        return memberService.leaveCommunity(accountNumber, customerId);
    }
}
