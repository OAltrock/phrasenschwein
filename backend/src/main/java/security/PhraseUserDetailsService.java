package security;

import models.PhraseUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import repository.PhraseUserRepository;

@Service
public class PhraseUserDetailsService implements UserDetailsService {

    private final PhraseUserRepository phraseUserRepository;

    public PhraseUserDetailsService(PhraseUserRepository phraseUserRepository) {
        this.phraseUserRepository = phraseUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        PhraseUser phraseUser = phraseUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new PhraseUserPrincipal(phraseUser);
    }
}
