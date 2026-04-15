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
    public boolean compensateStep(Long sagaInstanceId, String stepName) {
        return false;
    }

    @Override
    public SagaInstance getSagaInstance(Long sagaInstanceId) {
        return null;
    }

    @Override
    public void compensateSaga(Long sagaInstanceId) {

    }

    @Override
    public void failSaga(Long sagaInstanceId) {

    }

    @Override
    public void completeSaga(Long sagaInstanceId) {

    }
}
