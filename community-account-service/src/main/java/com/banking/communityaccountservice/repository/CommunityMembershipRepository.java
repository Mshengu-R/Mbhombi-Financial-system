package com.banking.communityaccountservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banking.communityaccountservice.entity.CommunityMembership;
import java.util.Optional;

public interface CommunityMembershipRepository extends JpaRepository<CommunityMembership, String>{

    Optional<CommunityMembership> findByCommunityId(String communityId);
    Optional<CommunityMembership> findByCommunityIdAndCustomerId(String communityId, String customerId);

}