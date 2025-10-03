package com.example.demo.runners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.demo.models.User;
import com.example.demo.repository.UserRepository;

@Component
public class UserTestRunner implements CommandLineRunner {
	@Autowired
	private UserRepository userRepo ; 

	@Override
	public void run(String... args) throws Exception {
		try {
			User user = new User() ; 
			user.setName("Ram");
			user.setEmail("ram@gmail.com");
			userRepo.save(user) ; 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

+