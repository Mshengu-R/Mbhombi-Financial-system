package com.banking.communityaccountservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banking.communityaccountservice.entity.CommunityAccount;

public interface ComunityMembershipRepository extends JpaRepository<CommunityAccount, String>{
   
    boolean existsBymemberId(String memberId);
    // boolean existsByAccountNumber(String );

}