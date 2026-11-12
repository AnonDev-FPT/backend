package com.anondev.platform.modules.candidate.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    // Identity Vault fields
    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    private String realFullName;

    @Email(message = "Invalid contact email format")
    private String contactEmail;

    @Pattern(regexp = "^(84|0[35789])\\d{8}$", message = "Contact phone must be a valid 10-digit Vietnamese phone number")
    private String contactPhone;

    @Size(max = 150, message = "University name cannot exceed 150 characters")
    private String universityName;

    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Gender must be MALE, FEMALE, or OTHER")
    private String gender;

    private String avatarUrl;
    private String originalCvUrl;
    private List<String> portfolioLinks;

    // Blind Profile fields
    @Size(max = 100, message = "Province/City cannot exceed 100 characters")
    private String provinceCity;

    @Pattern(regexp = "^(REMOTE|HYBRID|ONSITE|OFFLINE)$", message = "Work mode must be REMOTE, HYBRID, ONSITE, or OFFLINE")
    private String workMode;

    @Pattern(regexp = "^(INTERN|FRESHER|JUNIOR|MIDDLE|SENIOR|LEAD)$", message = "Experience level must be INTERN, FRESHER, JUNIOR, MIDDLE, SENIOR, or LEAD")
    private String experienceLevel;

    @PositiveOrZero(message = "Expected minimum salary must be greater than or equal to 0")
    private BigDecimal expectedSalaryMin;

    @PositiveOrZero(message = "Expected maximum salary must be greater than or equal to 0")
    private BigDecimal expectedSalaryMax;

    private List<String> primaryTechStack;

    @Size(max = 2000, message = "Bio cannot exceed 2000 characters")
    private String bio;
}
