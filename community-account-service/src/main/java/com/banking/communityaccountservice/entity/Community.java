package com.banking.communityaccountservice.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "communities")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Community {
   
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    // @Column(name = "description")
    // private String description;

    @Column(length = 3, nullable = false)
    private String currency;

    // CHANGED: join codes are created by the creator and are exactly six digits.
    @Column(nullable = false, length = 6)
    private String joinCode;
    
    @Column(name = "rules")
    private String rulesDescription;

    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CommunityMember> members = new HashSet<>();

    // @OneToOne(mappedBy = "community", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private CommunityAccount primaryAccount;
}
