package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.models.User;
import com.example.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service("userService")
@RequiredArgsConstructor
public class IUserServiceImpl implements IUserService {
	
	private final UserRepository userRepo ; 
	
	@Override
	public User createUser(User user) {
		User newUser = userRepo.save(user) ; 
		return newUser ; 
	}
	
	@Override
	public User getUserById(Long id) {
	    Optional<User> newUser = userRepo.findById(id);

	    if (newUser.isEmpty()) {
	        throw new RuntimeException("User not found with id: " + id);
	    }

	    return newUser.get();
	}

	
	@Override
	public List<User> getUsersByName(String name) {
		List<User> userList = userRepo.findByNameContainingIgnoreCase(name) ; 
		return userList ; 
	}

}
