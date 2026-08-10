package security;

import models.PhraseUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class PhraseUserPrincipal implements UserDetails {

    private final PhraseUser phraseUser;

    public PhraseUserPrincipal(PhraseUser phraseUser) {
        this.phraseUser = phraseUser;
    }

    public Long getId() {
        return phraseUser.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return phraseUser.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return phraseUser.getUsername();
    }
}
