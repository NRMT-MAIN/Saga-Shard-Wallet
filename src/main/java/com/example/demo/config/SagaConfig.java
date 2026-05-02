package com.example.demo.config;

import com.example.demo.service.saga.SagaStepInterface;
import com.example.demo.service.saga.steps.CreditDestinationWalletStep;
import com.example.demo.service.saga.steps.DebitSourceWalletStep;
import com.example.demo.service.saga.steps.UpdateTransactionStatusStep;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class SagaConfig {

    @Bean
    public Map<String , SagaStepInterface> SagaStepMap(DebitSourceWalletStep debitSourceWalletStep ,
                                                             CreditDestinationWalletStep creditDestinationWalletStep ,
                                                             UpdateTransactionStatusStep updateTransactionStatusStep ) {
        return Map.of(
            debitSourceWalletStep.getStepName() , debitSourceWalletStep,
            creditDestinationWalletStep.getStepName() , creditDestinationWalletStep,
            updateTransactionStatusStep.getStepName() , updateTransactionStatusStep
        );
    }
}
