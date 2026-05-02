package com.example.demo.service.saga.steps;

import com.example.demo.service.saga.SagaStepInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class SagaStepFactory {
    private final Map<String , SagaStepInterface> sagaStepMap;

    public enum SagaStepType {
        DEBIT_SOURCE_WALLET_STEP,
        CREDIT_DESTINATION_WALLET_STEP,
        UPDATE_TRANSACTION_STATUS_STEP
    }

    public SagaStepInterface getStep(String stepName) {

        return sagaStepMap.get(stepName);
    }
}
