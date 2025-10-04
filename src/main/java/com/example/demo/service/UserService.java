package com.example.demo.service;

import java.util.List;

import com.example.demo.models.User;

public interface UserService {
	public User createUser(User user) ; 
	public User getUserById(Long id) ; 
	public List<User> getUsersByName(String name) ; 
}
