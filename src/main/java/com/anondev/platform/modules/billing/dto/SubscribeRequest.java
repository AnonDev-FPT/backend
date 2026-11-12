package com.anondev.platform.modules.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscribeRequest {

    @NotBlank(message = "Plan code is required (BASIC, PRO, ULTRA)")
    @Pattern(regexp = "^(BASIC|PRO|ULTRA)$", message = "Plan code must be BASIC, PRO, or ULTRA")
    private String planCode;

    @NotBlank(message = "Billing cycle is required (MONTHLY, ANNUAL)")
    @Pattern(regexp = "^(MONTHLY|ANNUAL)$", message = "Billing cycle must be MONTHLY or ANNUAL")
    private String billingCycle;
}
