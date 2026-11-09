package com.anondev.platform.modules.billing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscribeRequest {
    @NotBlank
    private String planCode; // BASIC, PRO, ULTRA
    @NotBlank
    private String billingCycle; // MONTHLY, ANNUAL
}
