package service;

import dtos.AccountResetResponse;
import dtos.CreateUserRequest;
import dtos.UserSummary;
import exceptions.ResourceNotFoundException;
import exceptions.SelfAccountResetException;
import exceptions.SelfUserDeletionException;
import exceptions.UserHasBalanceException;
import exceptions.UsernameAlreadyExistsException;
import jakarta.transaction.Transactional;
import models.PhraseUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import repository.PhraseRepository;
import repository.PhraseUserRepository;

import java.math.BigDecimal;

@Service
@Validated
@Transactional
public class UserService {

    private final PhraseUserRepository phraseUserRepository;
    private final PhraseRepository phraseRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(PhraseUserRepository phraseUserRepository, PhraseRepository phraseRepository,
                        PasswordEncoder passwordEncoder) {
        this.phraseUserRepository = phraseUserRepository;
        this.phraseRepository = phraseRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserSummary createUser(CreateUserRequest request) {
        if (phraseUserRepository.findByUsername(request.username()).isPresent()) {
            throw new UsernameAlreadyExistsException("Username already taken: " + request.username());
        }

        PhraseUser user = new PhraseUser();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setAccountBalance(BigDecimal.ZERO);
        user.setAdmin(false);
        user = phraseUserRepository.save(user);

        return new UserSummary(user.getId(), user.getUsername());
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

    public void deleteUser(String username, Long actingUserId) {
        PhraseUser user = phraseUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (user.getId().equals(actingUserId)) {
            throw new SelfUserDeletionException("You cannot delete your own account");
        }

        if (user.getAccountBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new UserHasBalanceException(
                    "Cannot delete user with a non-zero account balance: " + user.getUsername());
        }

        phraseRepository.deleteByIssuerIdOrReceiverId(user.getId());
        phraseUserRepository.delete(user);
    }
}
