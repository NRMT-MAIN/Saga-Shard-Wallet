package com.example.demo.service;

import com.example.demo.models.Transaction;
import com.example.demo.service.saga.SagaContext;
import com.example.demo.service.saga.SagaOrchestator;
import com.example.demo.service.saga.steps.SagaStepFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransferSagaService {
    private final ITransactionService transactionService ;
    private final SagaOrchestator sagaOrchestator ;


    @Transactional
    public Long initiateTransfer(Long fromWalletId, Long toWalletId, BigDecimal amount, String description) {
        log.info("Initiating transfer from wallet {} to wallet {} with amount {}", fromWalletId, toWalletId, amount);

        Transaction transaction = transactionService.createTransaction(fromWalletId, toWalletId, amount, description);

        SagaContext sagaContext = SagaContext.builder()
                .data(Map.ofEntries(
                        Map.entry("transactionId", transaction.getId()),
                        Map.entry("fromWalletId", fromWalletId),
                        Map.entry("toWalletId", toWalletId),
                        Map.entry("amount", amount)
                ))
                .build();

        Long sagaInstanceId = sagaOrchestator.startSaga(sagaContext);
        log.info("Saga instance started with id {}", sagaInstanceId);

        transactionService.updateTransactionWithSagaInstanceId(transaction.getId(), sagaInstanceId);

        executeTransferSaga(sagaInstanceId);
        return sagaInstanceId ;
    }

    public void executeTransferSaga(Long sagaInstanceId) {
        log.info("Executing transfer saga with id {}", sagaInstanceId);

        try {
            for(SagaStepFactory.SagaStepType stepType : SagaStepFactory.SagaStepType.values()) {
                boolean stepResult = sagaOrchestator.executeStep(sagaInstanceId, stepType.name());
                if (!stepResult) {
                    log.error("Step {} failed for saga instance {}", stepType.name(), sagaInstanceId);
                    sagaOrchestator.failSaga(sagaInstanceId);
                    throw new RuntimeException("Step " + stepType.name() + " failed for saga instance " + sagaInstanceId);
                }
            }
            sagaOrchestator.completeSaga(sagaInstanceId);
            log.info("Transfer saga with id {} executed successfully", sagaInstanceId);
        } catch (Exception e) {
            log.error("Error executing transfer saga with id {}", sagaInstanceId, e);
            sagaOrchestator.completeSaga(sagaInstanceId);
            throw new RuntimeException("Error executing transfer saga with id " + sagaInstanceId, e);
        }
    }
}
