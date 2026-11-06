package com.anondev.platform.modules.candidate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentityVaultDto {
    private String realFullName;
    private String contactEmail;
    private String contactPhone;
    private String universityName;
    private String gender;
    private String avatarUrl;
    private String originalCvUrl;
    private String sanitizedCvUrl;
    private List<String> portfolioLinks;
}
