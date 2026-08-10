package service;

import com.convales.phrasenschwein.PhrasenSchweinApplication;
import com.convales.phrasenschwein.TestcontainersConfiguration;
import dtos.AccountResetResponse;
import dtos.CreateUserRequest;
import dtos.UserSummary;
import exceptions.ResourceNotFoundException;
import exceptions.SelfAccountResetException;
import exceptions.UsernameAlreadyExistsException;
import models.PhraseUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import repository.PhraseRepository;
import repository.PhraseUserRepository;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = PhrasenSchweinApplication.class)
@Import(TestcontainersConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceTest {

    @Autowired
    private UserService userService;
    @Autowired
    private PhraseUserRepository phraseUserRepository;
    @Autowired
    private PhraseRepository phraseRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private PhraseUser actor;
    private PhraseUser user;

    @BeforeEach
    void setUp() {
        phraseRepository.deleteAll();
        phraseUserRepository.deleteAll();

        actor = new PhraseUser();
        actor.setUsername("actor");
        actor.setPasswordHash(passwordEncoder.encode("password"));
        actor.setAccountBalance(BigDecimal.ZERO);
        actor = phraseUserRepository.save(actor);

        user = new PhraseUser();
        user.setUsername("victim");
        user.setPasswordHash(passwordEncoder.encode("password"));
        user.setAccountBalance(new BigDecimal("13.50"));
        user = phraseUserRepository.save(user);
    }

    @Test
    void resetAccountBalanceSetsBalanceToZero() {
        AccountResetResponse response = userService.resetAccountBalance(user.getUsername(), actor.getId());

        assertThat(response.username()).isEqualTo("victim");
        assertThat(response.accountBalance()).isEqualByComparingTo(BigDecimal.ZERO);

        PhraseUser updated = phraseUserRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getAccountBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void resetAccountBalanceThrowsWhenUserDoesNotExist() {
        assertThrows(ResourceNotFoundException.class, () ->
                userService.resetAccountBalance("does-not-exist", actor.getId()));
    }

    @Test
    void resetAccountBalanceThrowsWhenActorResetsOwnAccount() {
        assertThrows(SelfAccountResetException.class, () ->
                userService.resetAccountBalance(user.getUsername(), user.getId()));

        PhraseUser unchanged = phraseUserRepository.findById(user.getId()).orElseThrow();
        assertThat(unchanged.getAccountBalance()).isEqualByComparingTo("13.50");
    }

    @Test
    void createUserPersistsNewNonAdminUserWithZeroBalance() {
        UserSummary response = userService.createUser(new CreateUserRequest("newbie", "password123"));

        assertThat(response.username()).isEqualTo("newbie");

        PhraseUser created = phraseUserRepository.findByUsername("newbie").orElseThrow();
        assertThat(created.getId()).isEqualTo(response.id());
        assertThat(created.isAdmin()).isFalse();
        assertThat(created.getAccountBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(passwordEncoder.matches("password123", created.getPasswordHash())).isTrue();
    }

    @Test
    void createUserThrowsWhenUsernameAlreadyTaken() {
        assertThrows(UsernameAlreadyExistsException.class, () ->
                userService.createUser(new CreateUserRequest(actor.getUsername(), "password123")));

        assertThat(phraseUserRepository.count()).isEqualTo(2);
    }
}
