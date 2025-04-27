package th.ac.mahidol.ict.Gemini9.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import th.ac.mahidol.ict.Gemini9.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}