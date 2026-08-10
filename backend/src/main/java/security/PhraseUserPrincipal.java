package security;

import models.PhraseUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
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

    public boolean isAdmin() {
        return phraseUser.isAdmin();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        if (phraseUser.isAdmin()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return authorities;
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
