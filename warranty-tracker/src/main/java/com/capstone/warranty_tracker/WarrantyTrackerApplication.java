package com.capstone.warranty_tracker;

import com.capstone.warranty_tracker.model.Admin;
import com.capstone.warranty_tracker.model.Role;
import com.capstone.warranty_tracker.model.Technician;
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

			if (!userRepository.existsByEmail("tech@example.com")) {
				Technician tech = new Technician();
				tech.setUsername("TechUser");
				tech.setEmail("tech@example.com");
				tech.setPassword(passwordEncoder.encode("tech123"));
				tech.setRole(Role.ROLE_TECHNICIAN);
				tech.setFirstName("Tech");
				tech.setLastName("One");
				tech.setPhoneNumber("9876543210");
				tech.setSpecialization("Electronics");
				tech.setExperience(3);
				userRepository.save(tech);
				System.out.println("✅ Technician user created: tech1@example.com / tech123");
			}

		}
	}
	public static void main(String[] args) {
		SpringApplication.run(WarrantyTrackerApplication.class, args);
	}

}
