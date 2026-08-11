package controller;

import com.convales.phrasenschwein.PhrasenSchweinApplication;
import com.convales.phrasenschwein.TestcontainersConfiguration;
import dtos.AccountResetRequest;
import dtos.AccountResetResponse;
import dtos.CreateUserRequest;
import dtos.LoginRequest;
import dtos.LoginResponse;
import dtos.UserSummary;
import models.PhraseUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import repository.PhraseLikeRepository;
import repository.PhraseRepository;
import repository.PhraseUserRepository;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = PhrasenSchweinApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private PhraseUserRepository phraseUserRepository;
    @Autowired
    private PhraseRepository phraseRepository;
    @Autowired
    private PhraseLikeRepository phraseLikeRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private RestClient restClient;
    private PhraseUser actor;
    private PhraseUser victim;
    private PhraseUser admin;
    private String token;
    private String adminToken;

    @BeforeEach
    void setUp() {
        phraseLikeRepository.deleteAll();
        phraseRepository.deleteAll();
        phraseUserRepository.deleteAll();

        actor = new PhraseUser();
        actor.setUsername("actor");
        actor.setPasswordHash(passwordEncoder.encode("password"));
        actor.setAccountBalance(new BigDecimal("4.00"));
        actor = phraseUserRepository.save(actor);

        victim = new PhraseUser();
        victim.setUsername("victim");
        victim.setPasswordHash(passwordEncoder.encode("password"));
        victim.setAccountBalance(new BigDecimal("7.50"));
        phraseUserRepository.save(victim);

        admin = new PhraseUser();
        admin.setUsername("admin-user");
        admin.setPasswordHash(passwordEncoder.encode("password"));
        admin.setAccountBalance(BigDecimal.ZERO);
        admin.setAdmin(true);
        phraseUserRepository.save(admin);

        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();

        LoginResponse login = restClient.post()
                .uri("/api/auth/login")
                .body(new LoginRequest("actor", "password"))
                .retrieve()
                .body(LoginResponse.class);
        token = login.token();
        assertThat(login.admin()).isFalse();

        LoginResponse adminLogin = restClient.post()
                .uri("/api/auth/login")
                .body(new LoginRequest("admin-user", "password"))
                .retrieve()
                .body(LoginResponse.class);
        adminToken = adminLogin.token();
        assertThat(adminLogin.admin()).isTrue();
    }

    @Test
    void resetAccountOverHttpZeroesBalanceAndReturns200() {
        AccountResetResponse response = restClient.post()
                .uri("/api/users/reset")
                .header("Authorization", "Bearer " + adminToken)
                .body(new AccountResetRequest(victim.getUsername()))
                .retrieve()
                .body(AccountResetResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.username()).isEqualTo("victim");
        assertThat(response.accountBalance()).isEqualByComparingTo(BigDecimal.ZERO);

        PhraseUser updatedVictim = phraseUserRepository.findById(victim.getId()).orElseThrow();
        assertThat(updatedVictim.getAccountBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void resetAccountWithoutTokenIsRejected() {
        RestClientResponseException ex = assertThrows(
                RestClientResponseException.class,
                () -> restClient.post()
                        .uri("/api/users/reset")
                        .body(new AccountResetRequest(victim.getUsername()))
                        .retrieve()
                        .body(AccountResetResponse.class));

        assertThat(ex.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void resetAccountAsNonAdminIsForbidden() {
        RestClientResponseException ex = assertThrows(
                RestClientResponseException.class,
                () -> restClient.post()
                        .uri("/api/users/reset")
                        .header("Authorization", "Bearer " + token)
                        .body(new AccountResetRequest(victim.getUsername()))
                        .retrieve()
                        .body(AccountResetResponse.class));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        PhraseUser unchangedVictim = phraseUserRepository.findById(victim.getId()).orElseThrow();
        assertThat(unchangedVictim.getAccountBalance()).isEqualByComparingTo("7.50");
    }

    @Test
    void resetAccountWithUnknownUserReturns404() {
        RestClientResponseException ex = assertThrows(
                RestClientResponseException.class,
                () -> restClient.post()
                        .uri("/api/users/reset")
                        .header("Authorization", "Bearer " + adminToken)
                        .body(new AccountResetRequest("no-such-user"))
                        .retrieve()
                        .body(AccountResetResponse.class));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void resetOwnAccountAsAdminReturns400AndLeavesBalanceUnchanged() {
        RestClientResponseException ex = assertThrows(
                RestClientResponseException.class,
                () -> restClient.post()
                        .uri("/api/users/reset")
                        .header("Authorization", "Bearer " + adminToken)
                        .body(new AccountResetRequest(admin.getUsername()))
                        .retrieve()
                        .body(AccountResetResponse.class));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        PhraseUser updatedAdmin = phraseUserRepository.findById(admin.getId()).orElseThrow();
        assertThat(updatedAdmin.getAccountBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void createUserAsAdminReturns201AndPersistsNonAdminUser() {
        UserSummary response = restClient.post()
                .uri("/api/users")
                .header("Authorization", "Bearer " + adminToken)
                .body(new CreateUserRequest("newbie", "password123"))
                .retrieve()
                .body(UserSummary.class);

        assertThat(response).isNotNull();
        assertThat(response.username()).isEqualTo("newbie");

        PhraseUser created = phraseUserRepository.findByUsername("newbie").orElseThrow();
        assertThat(created.isAdmin()).isFalse();
    }

    @Test
    void createUserAsNonAdminIsForbidden() {
        RestClientResponseException ex = assertThrows(
                RestClientResponseException.class,
                () -> restClient.post()
                        .uri("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .body(new CreateUserRequest("newbie", "password123"))
                        .retrieve()
                        .body(UserSummary.class));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(phraseUserRepository.findByUsername("newbie")).isEmpty();
    }

    @Test
    void createUserWithoutTokenIsRejected() {
        RestClientResponseException ex = assertThrows(
                RestClientResponseException.class,
                () -> restClient.post()
                        .uri("/api/users")
                        .body(new CreateUserRequest("newbie", "password123"))
                        .retrieve()
                        .body(UserSummary.class));

        assertThat(ex.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void listUsersExcludesAdmins() {
        UserSummary[] users = restClient.get()
                .uri("/api/users")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(UserSummary[].class);

        assertThat(users).isNotNull();
        assertThat(users).extracting(UserSummary::username)
                .containsExactlyInAnyOrder("actor", "victim")
                .doesNotContain("admin-user");
    }

    @Test
    void createUserWithDuplicateUsernameAsAdminReturns409() {
        RestClientResponseException ex = assertThrows(
                RestClientResponseException.class,
                () -> restClient.post()
                        .uri("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .body(new CreateUserRequest(actor.getUsername(), "password123"))
                        .retrieve()
                        .body(UserSummary.class));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void deleteUserAsAdminReturns204AndRemovesUser() {
        PhraseUser deletable = new PhraseUser();
        deletable.setUsername("deletable");
        deletable.setPasswordHash(passwordEncoder.encode("password"));
        deletable.setAccountBalance(BigDecimal.ZERO);
        phraseUserRepository.save(deletable);

        restClient.delete()
                .uri("/api/users/{username}", deletable.getUsername())
                .header("Authorization", "Bearer " + adminToken)
                .retrieve()
                .toBodilessEntity();

        assertThat(phraseUserRepository.findByUsername("deletable")).isEmpty();
    }

    @Test
    void deleteUserWithNonZeroBalanceAsAdminReturns409() {
        RestClientResponseException ex = assertThrows(
                RestClientResponseException.class,
                () -> restClient.delete()
                        .uri("/api/users/{username}", victim.getUsername())
                        .header("Authorization", "Bearer " + adminToken)
                        .retrieve()
                        .toBodilessEntity());

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(phraseUserRepository.findByUsername("victim")).isPresent();
    }

    @Test
    void deleteUserAsNonAdminIsForbidden() {
        RestClientResponseException ex = assertThrows(
                RestClientResponseException.class,
                () -> restClient.delete()
                        .uri("/api/users/{username}", victim.getUsername())
                        .header("Authorization", "Bearer " + token)
                        .retrieve()
                        .toBodilessEntity());

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(phraseUserRepository.findByUsername("victim")).isPresent();
    }

    @Test
    void deleteUserWithoutTokenIsRejected() {
        RestClientResponseException ex = assertThrows(
                RestClientResponseException.class,
                () -> restClient.delete()
                        .uri("/api/users/{username}", victim.getUsername())
                        .retrieve()
                        .toBodilessEntity());

        assertThat(ex.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void deleteUnknownUserAsAdminReturns404() {
        RestClientResponseException ex = assertThrows(
                RestClientResponseException.class,
                () -> restClient.delete()
                        .uri("/api/users/{username}", "no-such-user")
                        .header("Authorization", "Bearer " + adminToken)
                        .retrieve()
                        .toBodilessEntity());

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void adminDeletingOwnAccountReturns400() {
        RestClientResponseException ex = assertThrows(
                RestClientResponseException.class,
                () -> restClient.delete()
                        .uri("/api/users/{username}", admin.getUsername())
                        .header("Authorization", "Bearer " + adminToken)
                        .retrieve()
                        .toBodilessEntity());

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(phraseUserRepository.findByUsername("admin-user")).isPresent();
    }
}
