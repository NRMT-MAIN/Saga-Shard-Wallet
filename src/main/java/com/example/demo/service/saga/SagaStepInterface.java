package com.example.demo.service.saga;

public interface SagaStepInterface {
	boolean execute(SagaContext context);

    boolean compensate(SagaContext context);

    String getStepName();
}
