package com.banking.communityaccountservice.controller;

import org.springframework.web.bind.annotation.RestController;

import com.banking.communityaccountservice.dto.CommunityAccountResponse;
import com.banking.communityaccountservice.dto.JoinCommunityRequest;
import com.banking.communityaccountservice.dto.JoinCommunityResponse;
import com.banking.communityaccountservice.service.CommunityMembershipService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/community-accounts")
@Slf4j
@RequiredArgsConstructor
public class CommunityMembershipController {
    
        private final CommunityMembershipService communityMembershipService;


        @PostMapping("/{accountNumber}/{customerId}/{joinCode}/join")
    public ResponseEntity<JoinCommunityResponse> joinCommunity(
            @PathVariable String accountNumber,
            @PathVariable String customerId,
            // @RequestParam String memberName,
            @PathVariable String joinCode,
            @Valid @RequestBody JoinCommunityRequest request) {
        return ResponseEntity.ok(communityMembershipService.joinCommunity(accountNumber, customerId, joinCode, request));
    }

    
    @PostMapping("/{accountNumber}/{id}/leave")
    public ResponseEntity<JoinCommunityResponse> leaveCommunity(
            @PathVariable String accountNumber,
            @PathVariable String id,
            @RequestParam String memberName) {
        return ResponseEntity.ok(communityMembershipService.leaveCommunity(accountNumber, id));
    }

    

}
