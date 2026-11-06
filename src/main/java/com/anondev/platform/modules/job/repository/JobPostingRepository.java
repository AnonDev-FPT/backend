package com.anondev.platform.modules.job.repository;

import com.anondev.platform.modules.job.entity.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, UUID> {
    List<JobPosting> findByCompanyId(UUID companyId);
    List<JobPosting> findByStatus(String status);
    long countByCompanyIdAndStatus(UUID companyId, String status);
}
