package service;

import com.convales.phrasenschwein.PhrasenSchweinApplication;
import com.convales.phrasenschwein.TestcontainersConfiguration;
import dtos.PhraseResponse;
import dtos.SanctionRequest;
import exceptions.ResourceNotFoundException;
import models.FineType;
import models.Phrase;
import models.PhraseUser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import repository.FineTypeRepository;
import repository.PhraseRepository;
import repository.PhraseUserRepository;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = PhrasenSchweinApplication.class)
@Import(TestcontainersConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PhraseServiceTest {

    @Autowired
    private PhraseService phraseService;
    @Autowired
    private PhraseUserRepository phraseUserRepository;
    @Autowired
    private PhraseRepository phraseRepository;
    @Autowired
    private FineTypeRepository fineTypeRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private PhraseUser issuer;
    private PhraseUser receiver;

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

        issuer = newUser("issuer");
        receiver = newUser("receiver");
    }

    private PhraseUser newUser(String username) {
        PhraseUser user = new PhraseUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode("password"));
        user.setAccountBalance(BigDecimal.ZERO);
        return phraseUserRepository.save(user);
    }

    @Test
    void sanctionCreditsReceiverByFineTypeAmount() {
        PhraseResponse response = phraseService.sanction(
                new SanctionRequest(receiver.getUsername(), FineType.Name.SCHWER, "receiver hat erwähnt, dass er jetzt ein 100x engineer ist"),
                issuer.getId());

        assertThat(response.amount()).isEqualByComparingTo("2.00");
        assertThat(response.issuer()).isEqualTo(issuer.getUsername());
        assertThat(response.receiver()).isEqualTo(receiver.getUsername());
        assertThat(response.type()).isEqualTo(FineType.Name.SCHWER);

        PhraseUser updatedReceiver = phraseUserRepository.findById(receiver.getId()).orElseThrow();
        assertThat(updatedReceiver.getAccountBalance()).isEqualByComparingTo("2.00");
    }

    @ParameterizedTest
    @EnumSource(FineType.Name.class)
    void sanctionUsesCorrectDefaultAmountPerFineType(FineType.Name fineTypeName) {
        phraseService.sanction(new SanctionRequest(receiver.getUsername(), fineTypeName, "x"), issuer.getId());

        PhraseUser updatedReceiver = phraseUserRepository.findById(receiver.getId()).orElseThrow();
        assertThat(updatedReceiver.getAccountBalance()).isEqualByComparingTo(fineTypeName.getDefaultAmount());
    }

    @Test
    void sanctionDoesNotCreditIssuerBalance() {
        phraseService.sanction(new SanctionRequest(receiver.getUsername(), FineType.Name.SCHWER, "x"), issuer.getId());

        PhraseUser updatedIssuer = phraseUserRepository.findById(issuer.getId()).orElseThrow();
        assertThat(updatedIssuer.getAccountBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void sanctionPersistsPhraseRecord() {
        phraseService.sanction(new SanctionRequest(receiver.getUsername(), FineType.Name.LEICHT, "receiver sagte: es ist wie es ist"), issuer.getId());

        assertThat(phraseRepository.count()).isEqualTo(1);
        Phrase phrase = phraseRepository.findAll().getFirst();
        assertThat(phrase.getIssuer().getId()).isEqualTo(issuer.getId());
        assertThat(phrase.getReceiver().getId()).isEqualTo(receiver.getId());
        // getFineType() is a lazy proxy; getId() doesn't trigger initialization, so look the row up separately
        // rather than opening a transaction across this whole test (which would race @BeforeEach's cleanup in other tests).
        FineType fineType = fineTypeRepository.findById(phrase.getFineType().getId()).orElseThrow();
        assertThat(fineType.getName()).isEqualTo(FineType.Name.LEICHT);
        assertThat(phrase.getText()).isEqualTo("receiver sagte: es ist wie es ist");
        assertThat(phrase.getIssuedAt()).isNotNull();
    }

    @Test
    void sanctionAllowsSelfSanction() {
        PhraseResponse response = phraseService.sanction(
                new SanctionRequest(issuer.getUsername(), FineType.Name.STANDARD, "receiver sagte: gemeinsam sind wir stark"),
                issuer.getId());

        assertThat(response.issuer()).isEqualTo(issuer.getUsername());
        assertThat(response.receiver()).isEqualTo(issuer.getUsername());

        PhraseUser updatedIssuer = phraseUserRepository.findById(issuer.getId()).orElseThrow();
        assertThat(updatedIssuer.getAccountBalance()).isEqualByComparingTo("1.00");
    }

    @Test
    void sanctionAccumulatesAcrossMultipleFines() {
        phraseService.sanction(new SanctionRequest(receiver.getUsername(), FineType.Name.LEICHT, "a"), issuer.getId());
        phraseService.sanction(new SanctionRequest(receiver.getUsername(), FineType.Name.STANDARD, "b"), issuer.getId());

        PhraseUser updatedReceiver = phraseUserRepository.findById(receiver.getId()).orElseThrow();
        assertThat(updatedReceiver.getAccountBalance()).isEqualByComparingTo("1.50");
        assertThat(phraseRepository.count()).isEqualTo(2);
    }

    @Test
    void sanctionThrowsWhenReceiverDoesNotExist() {
        assertThrows(ResourceNotFoundException.class, () ->
                phraseService.sanction(new SanctionRequest("does-not-exist", FineType.Name.LEICHT, "x"), issuer.getId()));
    }

    @Test
    void sanctionThrowsWhenIssuerDoesNotExist() {
        assertThrows(ResourceNotFoundException.class, () ->
                phraseService.sanction(new SanctionRequest(receiver.getUsername(), FineType.Name.LEICHT, "x"), -1L));
    }
}
