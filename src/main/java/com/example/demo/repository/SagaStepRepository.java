package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import com.example.demo.helpers.enums.SagaStatus;
import com.example.demo.helpers.enums.StepStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.models.SagaStep;
import org.springframework.stereotype.Repository;

@Repository
public interface SagaStepRepository extends JpaRepository<SagaStep, Long> {
	List<SagaStep> findBySagaInstanceId(Long sagaInstanceId);


    @Query("SELECT s FROM SagaStep s WHERE s.sagaInstanceId = :sagaInstanceId AND s.status = :status")
    List<SagaStep> findStepsBySagaInstanceIdAndStatus(@Param("sagaInstanceId") Long sagaInstanceId,
                                                      @Param("status") StepStatus status);


    @Query("SELECT s FROM SagaStep s WHERE s.sagaInstanceId = :sagaInstanceId AND s.status IN ('COMPLETED', 'COMPENSATED')")
    List<SagaStep> findCompletedOrCompensatedStepsBySagaInstanceId(@Param("sagaInstanceId") Long sagaInstanceId );

    Optional<SagaStep> findBySagaInstanceIdAndStepNameAndStatus(Long sagaInstanceId, String stepName, StepStatus status);
}

