package th.ac.mahidol.ict.Gemini_d6.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import th.ac.mahidol.ict.Gemini_d6.model.SciencePlan;
import th.ac.mahidol.ict.Gemini_d6.model.User;

import java.util.List;

@Repository
public interface SciencePlanRepository extends JpaRepository<SciencePlan, Long> {
    // Find plans by creator (useful for listing plans later)
    List<SciencePlan> findByCreator(User creator);
}
