package com.banking.communityaccountservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banking.communityaccountservice.entity.CommunityMembership;
import java.util.Optional;
import java.util.List;
import com.banking.communityaccountservice.entity.CommunityMembershipStatus;

public interface CommunityMemberRepository extends JpaRepository<CommunityMembership, String>{

    Optional<CommunityMembership> findByCommunityId(String communityId);
    Optional<CommunityMembership> findByCommunityIdAndCustomerId(String communityId, String customerId);

    // ADDED: recipients for community claim notifications.
    List<CommunityMembership> findByCommunityIdAndStatus(String communityId, CommunityMembershipStatus status);

}