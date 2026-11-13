package com.anondev.platform.modules.matching.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu phản hồi lời mời phỏng vấn từ ứng viên")
public class RespondInvitationRequest {

    @NotBlank(message = "Action is required")
    @Pattern(regexp = "^(ACCEPT|REJECT)$", message = "Action must be ACCEPT or REJECT")
    @Schema(description = "Hành động: ACCEPT (chấp nhận mở khóa hồ sơ) hoặc REJECT (từ chối lời mời)", example = "ACCEPT")
    private String action; // ACCEPT, REJECT
}

