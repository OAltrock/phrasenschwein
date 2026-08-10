package controller;

import com.convales.phrasenschwein.PhrasenSchweinApplication;
import com.convales.phrasenschwein.TestcontainersConfiguration;
import dtos.LoginRequest;
import dtos.LoginResponse;
import dtos.PhraseResponse;
import dtos.SanctionRequest;
import models.FineType;
import models.PhraseUser;
import org.junit.jupiter.api.BeforeAll;
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
import repository.FineTypeRepository;
import repository.PhraseRepository;
import repository.PhraseUserRepository;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = PhrasenSchweinApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PhraseControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private PhraseUserRepository phraseUserRepository;
    @Autowired
    private PhraseRepository phraseRepository;
    @Autowired
    private FineTypeRepository fineTypeRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private RestClient restClient;
    private PhraseUser receiver;
    private String token;

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
        phraseRepository.deleteAll();
        phraseUserRepository.deleteAll();

        PhraseUser issuer = new PhraseUser();
        issuer.setUsername("issuer");
        issuer.setPasswordHash(passwordEncoder.encode("password"));
        issuer.setAccountBalance(BigDecimal.ZERO);
        phraseUserRepository.save(issuer);

        receiver = new PhraseUser();
        receiver.setUsername("receiver");
        receiver.setPasswordHash(passwordEncoder.encode("password"));
        receiver.setAccountBalance(BigDecimal.ZERO);
        phraseUserRepository.save(receiver);

        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();

        LoginResponse login = restClient.post()
                .uri("/api/auth/login")
                .body(new LoginRequest("issuer", "password"))
                .retrieve()
                .body(LoginResponse.class);
        token = login.token();
    }

    @Test
    void sanctionOverHttpCreditsReceiverAndReturns201() {
        PhraseResponse response = restClient.post()
                .uri("/api/phrase/sanction")
                .header("Authorization", "Bearer " + token)
                .body(new SanctionRequest(receiver.getUsername(), FineType.Name.STANDARD, "Zu spät"))
                .retrieve()
                .body(PhraseResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.amount()).isEqualByComparingTo("1.00");
        assertThat(response.receiver()).isEqualTo("receiver");
        assertThat(response.issuer()).isEqualTo("issuer");

        PhraseUser updatedReceiver = phraseUserRepository.findById(receiver.getId()).orElseThrow();
        assertThat(updatedReceiver.getAccountBalance()).isEqualByComparingTo("1.00");
    }

    @Test
    void sanctionWithoutTokenIsRejected() {
        RestClientResponseException ex = org.junit.jupiter.api.Assertions.assertThrows(
                RestClientResponseException.class,
                () -> restClient.post()
                        .uri("/api/phrase/sanction")
                        .body(new SanctionRequest(receiver.getUsername(), FineType.Name.LEICHT, "x"))
                        .retrieve()
                        .body(PhraseResponse.class));

        assertThat(ex.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void sanctionWithUnknownReceiverReturns404() {
        RestClientResponseException ex = org.junit.jupiter.api.Assertions.assertThrows(
                RestClientResponseException.class,
                () -> restClient.post()
                        .uri("/api/phrase/sanction")
                        .header("Authorization", "Bearer " + token)
                        .body(new SanctionRequest("no-such-user", FineType.Name.LEICHT, "x"))
                        .retrieve()
                        .body(PhraseResponse.class));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void sanctionWithInvalidFineTypeStringReturns400() {
        RestClientResponseException ex = org.junit.jupiter.api.Assertions.assertThrows(
                RestClientResponseException.class,
                () -> restClient.post()
                        .uri("/api/phrase/sanction")
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .body("{\"username\":\"receiver\",\"type\":\"NOT_A_REAL_TYPE\",\"text\":\"x\"}")
                        .retrieve()
                        .body(PhraseResponse.class));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
