package service;

import com.convales.phrasenschwein.PhrasenSchweinApplication;
import com.convales.phrasenschwein.TestcontainersConfiguration;
import dtos.PhraseLikeResponse;
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
import repository.PhraseLikeRepository;
import repository.PhraseRepository;
import repository.PhraseUserRepository;

import java.math.BigDecimal;
import java.util.List;

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
    private PhraseLikeRepository phraseLikeRepository;
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
        phraseLikeRepository.deleteAll();
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

    @Test
    void recentSanctionsReturnsEmptyListWhenNoneExist() {
        assertThat(phraseService.recentSanctions(null)).isEmpty();
    }

    @Test
    void recentSanctionsReturnsMostRecentFirst() {
        phraseService.sanction(new SanctionRequest(receiver.getUsername(), FineType.Name.LEICHT, "erste"), issuer.getId());
        phraseService.sanction(new SanctionRequest(receiver.getUsername(), FineType.Name.STANDARD, "zweite"), issuer.getId());
        phraseService.sanction(new SanctionRequest(receiver.getUsername(), FineType.Name.SCHWER, "dritte"), issuer.getId());

        List<PhraseResponse> recent = phraseService.recentSanctions(null);

        assertThat(recent).hasSize(3);
        assertThat(recent.get(0).text()).isEqualTo("dritte");
        assertThat(recent.get(1).text()).isEqualTo("zweite");
        assertThat(recent.get(2).text()).isEqualTo("erste");
        assertThat(recent.get(0).receiver()).isEqualTo(receiver.getUsername());
    }

    @Test
    void recentSanctionsIsLimitedToTenMostRecent() {
        for (int i = 0; i < 12; i++) {
            phraseService.sanction(new SanctionRequest(receiver.getUsername(), FineType.Name.LEICHT, "phrase-" + i), issuer.getId());
        }

        List<PhraseResponse> recent = phraseService.recentSanctions(null);

        assertThat(recent).hasSize(10);
        assertThat(recent.get(0).text()).isEqualTo("phrase-11");
        assertThat(recent.get(9).text()).isEqualTo("phrase-2");
    }

    @Test
    void allSanctionsReturnsEmptyListWhenNoneExist() {
        assertThat(phraseService.allSanctions(null)).isEmpty();
    }

    @Test
    void allSanctionsWithNoLikesFallsBackToMostRecentFirstAndIsNotLimitedToTen() {
        for (int i = 0; i < 12; i++) {
            phraseService.sanction(new SanctionRequest(receiver.getUsername(), FineType.Name.LEICHT, "phrase-" + i), issuer.getId());
        }

        List<PhraseResponse> all = phraseService.allSanctions(null);

        assertThat(all).hasSize(12);
        assertThat(all.get(0).text()).isEqualTo("phrase-11");
        assertThat(all.get(11).text()).isEqualTo("phrase-0");
        assertThat(all).allSatisfy(response -> assertThat(response.likeCount()).isZero());
    }

    @Test
    void allSanctionsIsSortedByLikeCountDescending() {
        PhraseResponse least = phraseService.sanction(new SanctionRequest(receiver.getUsername(), FineType.Name.LEICHT, "least liked"), issuer.getId());
        PhraseResponse most = phraseService.sanction(new SanctionRequest(receiver.getUsername(), FineType.Name.LEICHT, "most liked"), issuer.getId());
        PhraseResponse middle = phraseService.sanction(new SanctionRequest(receiver.getUsername(), FineType.Name.LEICHT, "middle liked"), issuer.getId());

        PhraseUser secondLiker = newUser("second-liker");

        phraseService.toggleLike(most.id(), issuer.getId());
        phraseService.toggleLike(most.id(), secondLiker.getId());
        phraseService.toggleLike(middle.id(), issuer.getId());

        List<PhraseResponse> all = phraseService.allSanctions(null);

        assertThat(all).extracting(PhraseResponse::text)
                .containsExactly("most liked", "middle liked", "least liked");
        assertThat(all.get(0).likeCount()).isEqualTo(2);
        assertThat(all.get(1).likeCount()).isEqualTo(1);
        assertThat(all.get(2).likeCount()).isEqualTo(0);
    }

    @Test
    void allSanctionsReflectsLikedByCurrentUser() {
        PhraseResponse created = phraseService.sanction(new SanctionRequest(receiver.getUsername(), FineType.Name.LEICHT, "x"), issuer.getId());
        phraseService.toggleLike(created.id(), issuer.getId());

        List<PhraseResponse> asLiker = phraseService.allSanctions(issuer.getId());
        assertThat(asLiker.getFirst().likedByCurrentUser()).isTrue();

        List<PhraseResponse> asOtherUser = phraseService.allSanctions(receiver.getId());
        assertThat(asOtherUser.getFirst().likedByCurrentUser()).isFalse();

        List<PhraseResponse> asAnonymous = phraseService.allSanctions(null);
        assertThat(asAnonymous.getFirst().likedByCurrentUser()).isFalse();
    }

    @Test
    void toggleLikeAddsLikeAndIncrementsCount() {
        PhraseResponse created = phraseService.sanction(new SanctionRequest(receiver.getUsername(), FineType.Name.LEICHT, "x"), issuer.getId());

        PhraseLikeResponse response = phraseService.toggleLike(created.id(), issuer.getId());

        assertThat(response.likedByCurrentUser()).isTrue();
        assertThat(response.likeCount()).isEqualTo(1);
    }

    @Test
    void toggleLikeTwiceByTheSameUserRemovesTheLike() {
        PhraseResponse created = phraseService.sanction(new SanctionRequest(receiver.getUsername(), FineType.Name.LEICHT, "x"), issuer.getId());

        phraseService.toggleLike(created.id(), issuer.getId());
        PhraseLikeResponse secondToggle = phraseService.toggleLike(created.id(), issuer.getId());

        assertThat(secondToggle.likedByCurrentUser()).isFalse();
        assertThat(secondToggle.likeCount()).isZero();
    }

    @Test
    void toggleLikeIsIndependentPerUser() {
        PhraseResponse created = phraseService.sanction(new SanctionRequest(receiver.getUsername(), FineType.Name.LEICHT, "x"), issuer.getId());
        PhraseUser secondLiker = newUser("second-liker");

        phraseService.toggleLike(created.id(), issuer.getId());
        PhraseLikeResponse response = phraseService.toggleLike(created.id(), secondLiker.getId());

        assertThat(response.likedByCurrentUser()).isTrue();
        assertThat(response.likeCount()).isEqualTo(2);
    }

    @Test
    void toggleLikeThrowsWhenPhraseDoesNotExist() {
        assertThrows(ResourceNotFoundException.class, () ->
                phraseService.toggleLike(-1L, issuer.getId()));
    }
}
