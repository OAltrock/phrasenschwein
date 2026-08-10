package service;

import dtos.AccountResetResponse;
import exceptions.ResourceNotFoundException;
import exceptions.SelfAccountResetException;
import jakarta.transaction.Transactional;
import models.PhraseUser;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import repository.PhraseUserRepository;

import java.math.BigDecimal;

@Service
@Validated
@Transactional
public class UserService {

    private final PhraseUserRepository phraseUserRepository;

    public UserService(PhraseUserRepository phraseUserRepository) {
        this.phraseUserRepository = phraseUserRepository;
    }

    public AccountResetResponse resetAccountBalance(String username, Long actingUserId) {
        PhraseUser user = phraseUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (user.getId().equals(actingUserId)) {
            throw new SelfAccountResetException("You cannot reset your own account balance");
        }

        phraseUserRepository.resetAccountBalance(user.getId());

        return new AccountResetResponse(user.getId(), user.getUsername(), BigDecimal.ZERO);
    }
}
