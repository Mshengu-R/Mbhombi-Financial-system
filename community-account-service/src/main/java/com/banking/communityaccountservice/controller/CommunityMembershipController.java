package com.banking.communityaccountservice.controller;

import org.springframework.web.bind.annotation.RestController;

import com.banking.communityaccountservice.dto.CommunityAccountResponse;
import com.banking.communityaccountservice.service.CommunityAccountService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/community-accounts")
@Slf4j
@RequiredArgsConstructor
public class CommunityMembershipController {
    
        private final CommunityAccountService communityAccountService;


        @PostMapping("/{accountNumber}/{id}/join")
    public ResponseEntity<CommunityAccountResponse> joinCommunity(
            @PathVariable String accountNumber,
            @PathVariable String id,
            @RequestParam String memberName,
            @RequestParam(required = false) String joinCode) {
        return ResponseEntity.ok(communityAccountService.joinCommunity(accountNumber, memberName, joinCode));
    }

    
    @PostMapping("/{accountNumber}/{id}/leave")
    public ResponseEntity<CommunityAccountResponse> leaveCommunity(
            @PathVariable String accountNumber,
            @RequestParam String memberName) {
        return ResponseEntity.ok(communityAccountService.leaveCommunity(accountNumber, memberName));
    }

    

}
