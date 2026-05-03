package com.example.demo.config;

import com.example.demo.service.saga.SagaStepInterface;
import com.example.demo.service.saga.steps.CreditDestinationWalletStep;
import com.example.demo.service.saga.steps.DebitSourceWalletStep;
import com.example.demo.service.saga.steps.SagaStepFactory.SagaStepType;
import com.example.demo.service.saga.steps.UpdateTransactionStatusStep;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SagaConfig {

    @Bean
    public Map<String, SagaStepInterface> sagaStepMap(
            DebitSourceWalletStep debitSourceWalletStep,
            CreditDestinationWalletStep creditDestinationWalletStep,
            UpdateTransactionStatusStep updateTransactionStatus
    ) {
        Map<String, SagaStepInterface> map = new HashMap<>();
        map.put(SagaStepType.DEBIT_SOURCE_WALLET_STEP.toString(), debitSourceWalletStep);
        map.put(SagaStepType.CREDIT_DESTINATION_WALLET_STEP.toString(), creditDestinationWalletStep);
        map.put(SagaStepType.UPDATE_TRANSACTION_STATUS_STEP.toString(), updateTransactionStatus);
        return map;
    }
}
