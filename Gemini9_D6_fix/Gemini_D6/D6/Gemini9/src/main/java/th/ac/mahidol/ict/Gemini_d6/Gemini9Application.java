package th.ac.mahidol.ict.Gemini_d6;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder; // Import PasswordEncoder
import th.ac.mahidol.ict.Gemini_d6.model.User;
import th.ac.mahidol.ict.Gemini_d6.repository.UserRepository;

@SpringBootApplication
public class Gemini9Application {

	public static void main(String[] args) {
		SpringApplication.run(Gemini9Application.class, args);
	}


	@Bean
	CommandLineRunner run(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			// Example: Admin User
			if (userRepository.findByUsername("admin").isEmpty()) {
				User adminUser = new User();
				adminUser.setUsername("admin");
				adminUser.setPassword(passwordEncoder.encode("adminpass"));
				adminUser.setRole("ROLE_ADMIN"); // Assign role
				userRepository.save(adminUser);
				System.out.println("Created admin user: admin / adminpass");
			}

			// Example: Astronomer User
			if (userRepository.findByUsername("astro1").isEmpty()) {
				User astroUser = new User();
				astroUser.setUsername("astro1");
				astroUser.setPassword(passwordEncoder.encode("astroPa55"));
				astroUser.setRole("ROLE_ASTRONOMER"); // Assign role
				userRepository.save(astroUser);
				System.out.println("Created astronomer user: astro1 / astroPa55");
			}

			// Example: Science Observer User
			if (userRepository.findByUsername("sciObs1").isEmpty()) {
				User sciObserver = new User();
				sciObserver.setUsername("sciObs1");
				sciObserver.setPassword(passwordEncoder.encode("sciPa55w0rd"));
				sciObserver.setRole("ROLE_SCIENCE_OBSERVER"); // Assign role
				userRepository.save(sciObserver);
				System.out.println("Created science observer user: sciObs1 / sciPa55w0rd");
			}
		};
	}
}