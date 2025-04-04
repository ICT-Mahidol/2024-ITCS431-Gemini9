package th.ac.mahidol.ict.Gemini9.service;

import org.springframework.beans.factory.annotation.Autowired;
// --- NEW: Import GrantedAuthority and SimpleGrantedAuthority ---
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import th.ac.mahidol.ict.Gemini9.model.User;
import th.ac.mahidol.ict.Gemini9.repository.UserRepository;
// --- NEW: Import Collections ---
import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));


        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole());


        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(authority)
        );
    }
}