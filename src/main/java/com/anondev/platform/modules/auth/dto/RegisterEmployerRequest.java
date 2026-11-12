package com.anondev.platform.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterEmployerRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^(84|0[35789])\\d{8}$", message = "Invalid Vietnamese phone number")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must have at least 6 characters")
    private String password;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Tax ID is required")
    @Pattern(regexp = "^\\d{10}(-\\d{3})?$", message = "Tax ID must be 10 digits or 13 digits (format: 0123456789 or 0123456789-001)")
    private String taxId;

    private String businessLicenseUrl;
}
