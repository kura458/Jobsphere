package com.jobsphere.jobsite.repository.seeker;

import com.jobsphere.jobsite.model.seeker.JobAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobAlertRepository extends JpaRepository<JobAlert, UUID> {
    List<JobAlert> findBySeekerId(UUID seekerId);

    List<JobAlert> findByIsActiveTrue();
}
