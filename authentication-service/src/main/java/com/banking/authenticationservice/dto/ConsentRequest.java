package com.banking.authenticationservice.dto;

import com.banking.authenticationservice.entity.ConsentType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConsentRequest {
    @NotNull
    private ConsentType consentType;

    @NotBlank
    private String documentVersion;
}