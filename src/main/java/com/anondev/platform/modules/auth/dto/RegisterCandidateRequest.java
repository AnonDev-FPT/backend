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
public class RegisterCandidateRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^(84|0[35789])\\d{8}$", message = "Invalid Vietnamese phone number")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 50, message = "Password must have between 6 and 50 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    private String realFullName;

    private String provinceCity;

    @Pattern(regexp = "^(REMOTE|ONSITE|OFFLINE|HYBRID)$", message = "Work mode must be REMOTE, ONSITE, OFFLINE, or HYBRID")
    private String workMode; // REMOTE, OFFLINE, HYBRID
    private String originalCvUrl;
}
