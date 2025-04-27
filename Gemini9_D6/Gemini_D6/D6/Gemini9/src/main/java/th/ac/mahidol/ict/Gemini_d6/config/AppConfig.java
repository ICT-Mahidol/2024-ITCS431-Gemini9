package th.ac.mahidol.ict.Gemini_d6.config;

import edu.gemini.app.ocs.OCS;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class AppConfig {

    @Bean
    @Scope("singleton")
    public OCS ocsInstance() {
        System.out.println("--- Creating Singleton OCS Bean (Test Mode: true) ---");
        return new OCS(true);
    }
}
