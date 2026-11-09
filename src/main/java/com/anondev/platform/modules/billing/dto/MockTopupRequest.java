package com.anondev.platform.modules.billing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockTopupRequest {
    @NotNull
    @DecimalMin("10000.00")
    private BigDecimal amount;
}
