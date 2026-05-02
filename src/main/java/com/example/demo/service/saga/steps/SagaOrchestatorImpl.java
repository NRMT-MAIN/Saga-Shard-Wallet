package com.example.demo.service.saga.steps;

import com.example.demo.helpers.enums.SagaStatus;
import com.example.demo.helpers.enums.StepStatus;
import com.example.demo.models.SagaInstance;
import com.example.demo.models.SagaStep;
import com.example.demo.repository.SagaInstanceRepository;
import com.example.demo.repository.SagaStepRepository;
import com.example.demo.service.saga.SagaContext;
import com.example.demo.service.saga.SagaOrchestator;
import com.example.demo.service.saga.SagaStepInterface;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestatorImpl implements SagaOrchestator {
    private final ObjectMapper objectMapper;
    private final SagaInstanceRepository sagaInstanceRepository;
    private final SagaStepRepository sagaStepRepository;
    private final SagaStepFactory sagaStepFactory;

    @Override
    @Transactional
    public Long startSaga(SagaContext context) {
        try {
            // Convert the context to JSON as a string
            String contextJson = objectMapper.writeValueAsString(context);
            SagaInstance sagaInstance = SagaInstance.builder()
                    .context(contextJson)
                    .status(SagaStatus.STARTED)
                    .build();

            sagaInstance = sagaInstanceRepository.save(sagaInstance);
            log.info("Saga instance created with id {}", sagaInstance.getId());
            return sagaInstance.getId();

        } catch (Exception e) {
            log.error("Error starting the saga", e) ;
            throw new RuntimeException("Error starting the saga", e);
        }
    }

    @Override
    @Transactional
    public boolean executeStep(Long sagaInstanceId, String stepName) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found with id: " + sagaInstanceId));

        SagaStepInterface step = sagaStepFactory.getStep(stepName);
        if (step == null) {
            log.error("Saga step not found with name: {}", stepName);
            throw new RuntimeException("Saga step not found with name: " + stepName);
        }

        SagaStep sagaStep = sagaStepRepository
                .findBySagaInstanceIdAndStepNameAndStatus(sagaInstanceId, stepName, StepStatus.PENDING)
                .orElse(
                        SagaStep.builder().sagaInstanceId(sagaInstanceId).stepName(stepName).status(StepStatus.PENDING).build()
                );

        if(sagaStep.getId() == null) {
            sagaStep = sagaStepRepository.save(sagaStep);
        }

        try {
            SagaContext context = objectMapper.readValue(sagaInstance.getContext(), SagaContext.class);
            sagaStep.markAsRunning();
            sagaStepRepository.save(sagaStep);

            boolean success = step.execute(context);

            if (success) {
                sagaStep.markAsCompleted();
                sagaStepRepository.save(sagaStep);

                sagaInstance.setCurrentStep(stepName);
                sagaInstance.setStatus(SagaStatus.RUNNING);
                sagaInstanceRepository.save(sagaInstance);
                log.info("Saga step {} executed successfully for saga instance {}", stepName, sagaInstanceId);
                return true;
            } else {
                sagaStep.markAsFailed();
                sagaStepRepository.save(sagaStep);
                log.error("Saga step {} failed for saga instance {}", stepName, sagaInstanceId);
                return false;
            }

        } catch (Exception e) {
            // Not sequentially compensating here, just marking the step as failed. Compensation will be handled separately.
            sagaStep.markAsFailed();
            sagaStepRepository.save(sagaStep);
            log.error("Error executing saga step {} for saga instance {}", stepName, sagaInstanceId, e);
            return false ;
        }
    }

    @Override
    @Transactional
    public boolean compensateStep(Long sagaInstanceId, String stepName) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found with id: " + sagaInstanceId));

        SagaStepInterface step = sagaStepFactory.getStep(stepName);
        if (step == null) {
            log.error("Saga step not found with name: {}", stepName);
            throw new RuntimeException("Saga step not found with name: " + stepName);
        }

        SagaStep sagaStep = sagaStepRepository
                .findBySagaInstanceIdAndStepNameAndStatus(sagaInstanceId, stepName, StepStatus.COMPLETED)
                .orElse(
                       null
                );

        if(sagaStep.getId() == null) {
            log.warn("Saga step {} for saga instance {} was never completed, skipping compensation", stepName, sagaInstanceId);
            return true ; // If the step was never completed, we can consider it as already compensated
        }

        try {
            SagaContext context = objectMapper.readValue(sagaInstance.getContext(), SagaContext.class);
            sagaStep.markAsCompensating();
            sagaStepRepository.save(sagaStep);

            boolean success = step.compensate(context);

            if (success) {
                sagaStep.markAsCompensated();
                sagaStepRepository.save(sagaStep);

                log.info("Saga step {} compensated successfully for saga instance {}", stepName, sagaInstanceId);
                return true;
            } else {
                sagaStep.markAsFailed();
                sagaStepRepository.save(sagaStep);
                log.error("Saga step {} failed for saga instance {}", stepName, sagaInstanceId);
                return false;
            }

        } catch (Exception e) {
            // Not sequentially compensating here, just marking the step as failed. Compensation will be handled separately.
            sagaStep.markAsFailed();
            sagaStepRepository.save(sagaStep);
            log.error("Error compensating saga step {} for saga instance {}", stepName, sagaInstanceId, e);
            return false ;
        }
    }

    @Override
    public SagaInstance getSagaInstance(Long sagaInstanceId) {
        return sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found with id: " + sagaInstanceId));
    }

    @Override
    @Transactional
    public void compensateSaga(Long sagaInstanceId) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found with id: " + sagaInstanceId));

        // Mark the saga instance as compensating
        sagaInstance.setStatus(SagaStatus.COMPENSATING);
        sagaInstanceRepository.save(sagaInstance);

        // Fetch all steps for the saga instance that are either completed or running, and compensate them in reverse order
        List<SagaStep> stepsToCompensate = sagaStepRepository.findCompletedOrCompensatedStepsBySagaInstanceId(sagaInstanceId) ;

        boolean allCompensated = true ;
        for (SagaStep step : stepsToCompensate) {
            boolean compensated = this.compensateStep(sagaInstanceId, step.getStepName());
            if (!compensated) {
                allCompensated = false;
                log.error("Failed to compensate step {} for saga instance {}", step.getStepName(), sagaInstanceId);
            } else {
                log.info("Successfully compensated step {} for saga instance {}", step.getStepName(), sagaInstanceId);
            }
        }

        if (allCompensated) {
            sagaInstance.setStatus(SagaStatus.COMPENSATED);
            log.info("Saga instance {} fully compensated", sagaInstanceId);
        } else {
            log.error("Saga instance {} compensation failed", sagaInstanceId);
        }
    }

    @Override
    @Transactional
    public void failSaga(Long sagaInstanceId) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found with id: " + sagaInstanceId));

        sagaInstance.setStatus(SagaStatus.FAILED);
        sagaInstanceRepository.save(sagaInstance);
        log.info("Saga instance {} marked as failed", sagaInstanceId);
    }

    @Override
    @Transactional
    public void completeSaga(Long sagaInstanceId) {
        	SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                    .orElseThrow(() -> new RuntimeException("Saga instance not found with id: " + sagaInstanceId));

            sagaInstance.setStatus(SagaStatus.COMPLETED);
            sagaInstanceRepository.save(sagaInstance);
            log.info("Saga instance {} marked as completed", sagaInstanceId);
    }
}
