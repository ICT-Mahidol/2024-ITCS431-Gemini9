package th.ac.mahidol.ict.Gemini_d6.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import th.ac.mahidol.ict.Gemini_d6.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}