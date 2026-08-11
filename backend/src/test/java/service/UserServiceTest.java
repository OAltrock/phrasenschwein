package service;

import com.convales.phrasenschwein.PhrasenSchweinApplication;
import com.convales.phrasenschwein.TestcontainersConfiguration;
import dtos.AccountResetResponse;
import dtos.CreateUserRequest;
import dtos.UserSummary;
import exceptions.ResourceNotFoundException;
import exceptions.SelfAccountResetException;
import exceptions.SelfUserDeletionException;
import exceptions.UserHasBalanceException;
import exceptions.UsernameAlreadyExistsException;
import models.FineType;
import models.Phrase;
import models.PhraseLike;
import models.PhraseUser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import repository.FineTypeRepository;
import repository.PhraseLikeRepository;
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
    private PhraseLikeRepository phraseLikeRepository;
    @Autowired
    private FineTypeRepository fineTypeRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private PhraseUser actor;
    private PhraseUser user;
    private PhraseUser zeroBalanceUser;

    @BeforeAll
    void seedFineTypes() {
        for (FineType.Name name : FineType.Name.values()) {
            if (fineTypeRepository.findByName(name).isEmpty()) {
                FineType fineType = new FineType();
                fineType.setName(name);
                fineTypeRepository.save(fineType);
            }
        }
    }

    @BeforeEach
    void setUp() {
        phraseLikeRepository.deleteAll();
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

        zeroBalanceUser = new PhraseUser();
        zeroBalanceUser.setUsername("zerobalance");
        zeroBalanceUser.setPasswordHash(passwordEncoder.encode("password"));
        zeroBalanceUser.setAccountBalance(BigDecimal.ZERO);
        zeroBalanceUser = phraseUserRepository.save(zeroBalanceUser);
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
        long countBefore = phraseUserRepository.count();

        assertThrows(UsernameAlreadyExistsException.class, () ->
                userService.createUser(new CreateUserRequest(actor.getUsername(), "password123")));

        assertThat(phraseUserRepository.count()).isEqualTo(countBefore);
    }

    @Test
    void deleteUserRemovesUserWithNoHistory() {
        userService.deleteUser(zeroBalanceUser.getUsername(), actor.getId());

        assertThat(phraseUserRepository.findByUsername("zerobalance")).isEmpty();
    }

    @Test
    void deleteUserCascadesSanctionHistoryAsIssuerAndReceiver() {
        FineType fineType = fineTypeRepository.findByName(FineType.Name.STANDARD).orElseThrow();

        Phrase issuedByZeroBalanceUser = new Phrase();
        issuedByZeroBalanceUser.setIssuer(zeroBalanceUser);
        issuedByZeroBalanceUser.setReceiver(actor);
        issuedByZeroBalanceUser.setFineType(fineType);
        issuedByZeroBalanceUser.setText("zerobalance war es");
        phraseRepository.save(issuedByZeroBalanceUser);

        Phrase receivedByZeroBalanceUser = new Phrase();
        receivedByZeroBalanceUser.setIssuer(actor);
        receivedByZeroBalanceUser.setReceiver(zeroBalanceUser);
        receivedByZeroBalanceUser.setFineType(fineType);
        receivedByZeroBalanceUser.setText("actor war es");
        phraseRepository.save(receivedByZeroBalanceUser);

        assertThat(phraseRepository.count()).isEqualTo(2);

        userService.deleteUser(zeroBalanceUser.getUsername(), actor.getId());

        assertThat(phraseUserRepository.findByUsername("zerobalance")).isEmpty();
        assertThat(phraseRepository.count()).isEqualTo(0);
    }

    @Test
    void deleteUserCascadesLikesOnOwnSanctions() {
        FineType fineType = fineTypeRepository.findByName(FineType.Name.STANDARD).orElseThrow();

        Phrase issuedByZeroBalanceUser = new Phrase();
        issuedByZeroBalanceUser.setIssuer(zeroBalanceUser);
        issuedByZeroBalanceUser.setReceiver(actor);
        issuedByZeroBalanceUser.setFineType(fineType);
        issuedByZeroBalanceUser.setText("zerobalance war es");
        issuedByZeroBalanceUser = phraseRepository.save(issuedByZeroBalanceUser);

        PhraseLike like = new PhraseLike();
        like.setPhrase(issuedByZeroBalanceUser);
        like.setUser(actor);
        phraseLikeRepository.save(like);

        userService.deleteUser(zeroBalanceUser.getUsername(), actor.getId());

        assertThat(phraseUserRepository.findByUsername("zerobalance")).isEmpty();
        assertThat(phraseRepository.count()).isEqualTo(0);
        assertThat(phraseLikeRepository.count()).isEqualTo(0);
    }

    @Test
    void deleteUserRemovesLikesMadeByTheDeletedUser() {
        FineType fineType = fineTypeRepository.findByName(FineType.Name.STANDARD).orElseThrow();

        Phrase phrase = new Phrase();
        phrase.setIssuer(actor);
        phrase.setReceiver(user);
        phrase.setFineType(fineType);
        phrase.setText("actor war es");
        phrase = phraseRepository.save(phrase);

        PhraseLike like = new PhraseLike();
        like.setPhrase(phrase);
        like.setUser(zeroBalanceUser);
        phraseLikeRepository.save(like);

        userService.deleteUser(zeroBalanceUser.getUsername(), actor.getId());

        assertThat(phraseUserRepository.findByUsername("zerobalance")).isEmpty();
        assertThat(phraseRepository.count()).isEqualTo(1);
        assertThat(phraseLikeRepository.count()).isEqualTo(0);
    }

    @Test
    void deleteUserThrowsWhenUserDoesNotExist() {
        assertThrows(ResourceNotFoundException.class, () ->
                userService.deleteUser("does-not-exist", actor.getId()));
    }

    @Test
    void deleteUserThrowsWhenActorDeletesOwnAccount() {
        assertThrows(SelfUserDeletionException.class, () ->
                userService.deleteUser(actor.getUsername(), actor.getId()));

        assertThat(phraseUserRepository.findByUsername("actor")).isPresent();
    }

    @Test
    void deleteUserThrowsWhenUserHasNonZeroBalance() {
        assertThrows(UserHasBalanceException.class, () ->
                userService.deleteUser(user.getUsername(), actor.getId()));

        PhraseUser unchanged = phraseUserRepository.findByUsername("victim").orElseThrow();
        assertThat(unchanged.getAccountBalance()).isEqualByComparingTo("13.50");
    }
}
