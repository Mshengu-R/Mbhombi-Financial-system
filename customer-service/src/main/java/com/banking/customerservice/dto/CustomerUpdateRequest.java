package com.banking.customerservice.dto;

import java.util.ArrayList;
import java.util.Date;

// import jakarta.validation.constraints.NotBlank;
// import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerUpdateRequest {

    // @NotBlank
    private String phone;

    // @NotBlank
    private String surname;

    // @NotBlank
    private String email;

    // @NotBlank
    private String name;

    // @NotBlank
    private String customerLanguage;

    // @NotBlank
    private String gender;

    // @NotNull
    private Date dateOfBirth;

    private ArrayList<String> homeAdress = new ArrayList<>();

    // @NotBlank
    private String ethnicity;

    // @NotBlank
    private String country;
    
}
