package com.anondev.platform.modules.company.dto;

import jakarta.validation.constraints.NotBlank;
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
    private String taxId;

    private String businessLicenseUrl;
    private String companyName;
}
