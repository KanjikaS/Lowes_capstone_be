package com.capstone.warranty_tracker;

import com.capstone.warranty_tracker.model.Admin;
import com.capstone.warranty_tracker.model.Role;
import com.capstone.warranty_tracker.model.User;
import com.capstone.warranty_tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class WarrantyTrackerApplication {
	@Component
	public class DataSeeder implements CommandLineRunner {

		@Autowired
		private UserRepository userRepository;

		@Autowired
		private PasswordEncoder passwordEncoder;

		@Override
		public void run(String... args) {
			if (!userRepository.existsByEmail("admin@example.com")) {
				Admin admin = new Admin();
				admin.setUsername("Admin");
				admin.setEmail("admin@example.com");
				admin.setPassword(passwordEncoder.encode("admin123"));
				admin.setRole(Role.ROLE_ADMIN);
				admin.setFirstName("System");
				admin.setLastName("Admin");

				userRepository.save(admin);
				System.out.println("✅ Admin user created: admin@example.com / admin123");
			} else {
				System.out.println("ℹ️ Admin user already exists.");
			}
		}
	}
	public static void main(String[] args) {
		SpringApplication.run(WarrantyTrackerApplication.class, args);
	}

}
