package com.anondev.platform.modules.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu nạp tiền giả lập vào ví doanh nghiệp")
public class MockTopupRequest {

    @NotNull(message = "Top-up amount is required")
    @DecimalMin(value = "10000.00", message = "Minimum top-up amount is 10,000 VND")
    @DecimalMax(value = "1000000000.00", message = "Maximum top-up amount is 1,000,000,000 VND")
    @Schema(description = "Số tiền nạp (từ 10.000 đến 1.000.000.000 VNĐ)", example = "500000.00")
    private BigDecimal amount;
}

