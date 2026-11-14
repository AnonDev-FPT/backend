package com.anondev.platform.modules.assessment.service;

import com.anondev.platform.modules.assessment.dto.CreateTestRequest;
import com.anondev.platform.modules.assessment.dto.SubmitTestRequest;
import com.anondev.platform.modules.assessment.dto.TestAttemptSummaryDto;
import com.anondev.platform.modules.assessment.entity.TechnicalTest;

import java.util.UUID;

public interface AssessmentService {

    TechnicalTest getJobTestByUserEmail(UUID jobId, String userEmail);

    TestAttemptSummaryDto submitTestByUserEmail(UUID jobId, String userEmail, SubmitTestRequest request);

    TechnicalTest createTestByUserEmail(String userEmail, CreateTestRequest request);

    TechnicalTest getJobTest(UUID jobId, UUID candidateId);

    TechnicalTest createTest(UUID companyId, CreateTestRequest request);

    TestAttemptSummaryDto submitTest(UUID jobId, UUID candidateId, SubmitTestRequest request);
}
