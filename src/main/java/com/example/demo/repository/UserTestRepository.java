package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.models.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserTestRepository extends JpaRepository<User, Long> {

}
