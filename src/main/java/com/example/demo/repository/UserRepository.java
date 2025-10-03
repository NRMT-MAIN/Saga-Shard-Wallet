package com.example.demo.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.models.User;

public interface UserRepository extends JpaRepository<User,Long> {
	public List<User> findByNameContainingIgnoreCase(String name) ; 
}
