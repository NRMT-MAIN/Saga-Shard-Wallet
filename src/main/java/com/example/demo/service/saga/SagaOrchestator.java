package com.example.demo.service.saga;

import com.example.demo.models.SagaInstance;

public interface SagaOrchestator {
	Long startSaga(SagaContext context);
	   
	boolean executeStep(Long sagaInstanceId, String stepName);

	boolean compensateStep(Long sagaInstanceId, String stepName);

	SagaInstance getSagaInstance(Long sagaInstanceId);

	void compensateSaga(Long sagaInstanceId);

	void failSaga(Long sagaInstanceId);

	void completeSaga(Long sagaInstanceId);
}
