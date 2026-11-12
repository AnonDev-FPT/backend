package com.anondev.platform.modules.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KybVerificationRequest {

    @NotBlank(message = "Tax ID is required")
    @Pattern(regexp = "^\\d{10}(-\\d{3})?$", message = "Tax ID must be 10 digits or 13 digits (format: 0123456789 or 0123456789-001)")
    private String taxId;

    private String businessLicenseUrl;
    private String companyName;
}
