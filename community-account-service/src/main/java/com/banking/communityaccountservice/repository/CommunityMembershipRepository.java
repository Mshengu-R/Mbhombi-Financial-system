package com.banking.communityaccountservice.repository;

import com.banking.communityaccountservice.entity.CommunityMembership;
import com.banking.communityaccountservice.entity.CommunityMembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommunityMembershipRepository extends JpaRepository<CommunityMembership, String> {
    Optional<CommunityMembership> findByCommunityIdAndCustomerId(String communityId, String customerId);
    List<CommunityMembership> findByCommunityIdAndStatus(String communityId, CommunityMembershipStatus status);
    List<CommunityMembership> findByCommunityId(String communityId);
    void deleteByCommunityId(String communityId);
}
