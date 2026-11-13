package com.anondev.platform.modules.matching.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thẻ hồ sơ ứng viên ẩn danh trong Talent Pool (BR-01)")
public class BlindCandidateCardDto {

    @Schema(description = "ID của ứng viên (User ID)")
    private UUID candidateId;

    @Schema(description = "Mã định danh ẩn danh (ví dụ: CAND-12345)")
    private String candidateCode;

    @Schema(description = "Tỉnh/Thành phố sinh sống")
    private String provinceCity;

    @Schema(description = "Chế độ làm việc: REMOTE, OFFLINE, HYBRID")
    private String workMode;

    @Schema(description = "Cấp bậc kinh nghiệm: INTERN, FRESHER, JUNIOR, MIDDLE, SENIOR, LEAD")
    private String experienceLevel;

    @Schema(description = "Mức lương tối thiểu kỳ vọng (VNĐ)")
    private BigDecimal expectedSalaryMin;

    @Schema(description = "Mức lương tối đa kỳ vọng (VNĐ)")
    private BigDecimal expectedSalaryMax;

    @Schema(description = "Danh sách công nghệ chính")
    private List<String> primaryTechStack;

    @Schema(description = "Điểm số đạt được trong bài test kỹ thuật của Job")
    private BigDecimal testScore;

    @Schema(description = "Cờ cảnh báo nếu bài làm nghi vấn dùng AI trái phép")
    private Boolean aiCodeFlagged;

    @Schema(description = "ID bài test kỹ thuật đã đạt chuẩn (truyền vào khi gửi lời mời phỏng vấn)")
    private UUID testAttemptId;

    @Schema(description = "ID lời mời phỏng vấn nếu đã từng gửi lời mời, null nếu chưa gửi")
    private UUID invitationId;

    @Schema(description = "Trạng thái lời mời phỏng vấn: NONE, PENDING, ACCEPTED_UNLOCKED, REJECTED, EXPIRED")
    private String invitationStatus;

    @Schema(description = "true nếu hồ sơ đã được bung két sắt mở khóa thông tin thật")
    private Boolean isUnlocked;
}
