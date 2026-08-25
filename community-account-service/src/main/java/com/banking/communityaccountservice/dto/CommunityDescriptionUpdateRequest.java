package com.banking.communityaccountservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommunityDescriptionUpdateRequest {

    // ADDED: creator-only update payload for the existing group description.
    @NotBlank(message = "Description is required")
    private String description;

    private String rulesDescription;
}