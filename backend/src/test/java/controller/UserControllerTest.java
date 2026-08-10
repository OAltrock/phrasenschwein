package controller;

import com.convales.phrasenschwein.PhrasenSchweinApplication;
import com.convales.phrasenschwein.TestcontainersConfiguration;
import dtos.AccountResetRequest;
import dtos.AccountResetResponse;
import dtos.LoginRequest;
import dtos.LoginResponse;
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
    private PasswordEncoder passwordEncoder;

    private RestClient restClient;
    private PhraseUser actor;
    private PhraseUser victim;
    private String token;

    @BeforeEach
    void setUp() {
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

        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();

        LoginResponse login = restClient.post()
                .uri("/api/auth/login")
                .body(new LoginRequest("actor", "password"))
                .retrieve()
                .body(LoginResponse.class);
        token = login.token();
    }

    @Test
    void resetAccountOverHttpZeroesBalanceAndReturns200() {
        AccountResetResponse response = restClient.post()
                .uri("/api/users/reset")
                .header("Authorization", "Bearer " + token)
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
    void resetAccountWithUnknownUserReturns404() {
        RestClientResponseException ex = assertThrows(
                RestClientResponseException.class,
                () -> restClient.post()
                        .uri("/api/users/reset")
                        .header("Authorization", "Bearer " + token)
                        .body(new AccountResetRequest("no-such-user"))
                        .retrieve()
                        .body(AccountResetResponse.class));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void resetOwnAccountReturns400AndLeavesBalanceUnchanged() {
        RestClientResponseException ex = assertThrows(
                RestClientResponseException.class,
                () -> restClient.post()
                        .uri("/api/users/reset")
                        .header("Authorization", "Bearer " + token)
                        .body(new AccountResetRequest(actor.getUsername()))
                        .retrieve()
                        .body(AccountResetResponse.class));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        PhraseUser updatedActor = phraseUserRepository.findById(actor.getId()).orElseThrow();
        assertThat(updatedActor.getAccountBalance()).isEqualByComparingTo("4.00");
    }
}
