package com.anondev.platform.modules.company.dto;

import com.anondev.platform.modules.company.entity.Company;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyProfileResponse {
    private UUID id;
    private UUID ownerUserId;
    private String companyName;
    private String taxId;
    private String businessLicenseUrl;
    private Boolean isTaxVerified;
    private Boolean hasUsedFreeJob;
    private BigDecimal creditBalance;
    private BigDecimal heldCreditBalance;
    private Integer statusUpdateCounter;
    private Boolean isBlockedByOverdueStatus;
    private Instant createdAt;

    public static CompanyProfileResponse fromEntity(Company entity) {
        if (entity == null) {
            return null;
        }
        return CompanyProfileResponse.builder()
                .id(entity.getId())
                .ownerUserId(entity.getOwnerUserId())
                .companyName(entity.getCompanyName())
                .taxId(entity.getTaxId())
                .businessLicenseUrl(entity.getBusinessLicenseUrl())
                .isTaxVerified(entity.getIsTaxVerified())
                .hasUsedFreeJob(entity.getHasUsedFreeJob())
                .creditBalance(entity.getCreditBalance())
                .heldCreditBalance(entity.getHeldCreditBalance())
                .statusUpdateCounter(entity.getStatusUpdateCounter())
                .isBlockedByOverdueStatus(entity.getIsBlockedByOverdueStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

