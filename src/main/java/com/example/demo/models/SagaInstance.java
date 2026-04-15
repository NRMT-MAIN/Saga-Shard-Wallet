package com.example.demo.models;

import lombok.Builder;
import org.apache.calcite.model.JsonType;

import com.example.demo.helpers.enums.SagaStatus;
import com.example.demo.helpers.enums.StepStatus;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "saga_instance")
public class SagaInstance {
	 @Id
	 @GeneratedValue(strategy = GenerationType.IDENTITY)
	 private Long id;
	 
	 @Enumerated(EnumType.STRING)
	 @Column(name = "status")
	 private SagaStatus status = SagaStatus.STARTED;

	 @Type(JsonType.class)
	 @Column(name = "context", columnDefinition = "json")
	 private String context;

	 @Column(name = "current_step", nullable = false)
	 private String currentStep;
}
