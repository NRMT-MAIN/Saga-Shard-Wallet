package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.models.SagaInstance;

public interface SagaInstanceRepository extends JpaRepository<SagaInstance , Long> {

}
