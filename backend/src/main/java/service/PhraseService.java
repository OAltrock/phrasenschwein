package service;

import dtos.PhraseLikeResponse;
import dtos.PhraseResponse;
import dtos.SanctionRequest;
import exceptions.AdminCannotLikeException;
import exceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import models.FineType;
import models.Phrase;
import models.PhraseLike;
import models.PhraseUser;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import repository.FineTypeRepository;
import repository.PhraseLikeRepository;
import repository.PhraseRepository;
import repository.PhraseUserRepository;
import repository.PhraseWithLikeCount;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Validated
@Transactional
public class PhraseService {

    private final PhraseRepository phraseRepository;
    private final PhraseUserRepository phraseUserRepository;
    private final FineTypeRepository fineTypeRepository;
    private final PhraseLikeRepository phraseLikeRepository;

    public PhraseService(PhraseRepository phraseRepository,
                          PhraseUserRepository phraseUserRepository,
                          FineTypeRepository fineTypeRepository,
                          PhraseLikeRepository phraseLikeRepository) {
        this.phraseRepository = phraseRepository;
        this.phraseUserRepository = phraseUserRepository;
        this.fineTypeRepository = fineTypeRepository;
        this.phraseLikeRepository = phraseLikeRepository;
    }

    public PhraseResponse sanction(SanctionRequest request, Long issuerId) {
        PhraseUser issuer = phraseUserRepository.findById(issuerId)
                .orElseThrow(() -> new ResourceNotFoundException("Issuer not found"));
        PhraseUser receiver = phraseUserRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.username()));
        FineType fineType = fineTypeRepository.findByName(request.type())
                .orElseThrow(() -> new ResourceNotFoundException("Unknown fine type: " + request.type()));

        Phrase phrase = new Phrase();
        phrase.setIssuer(issuer);
        phrase.setReceiver(receiver);
        phrase.setFineType(fineType);
        phrase.setText(request.text());
        phrase = phraseRepository.save(phrase);

        BigDecimal amount = fineType.getName().getDefaultAmount();
        phraseUserRepository.addToAccountBalance(receiver.getId(), amount);

        return toResponse(phrase, 0L, false);
    }

    public List<PhraseResponse> recentSanctions(Long currentUserId) {
        List<PhraseWithLikeCount> rows =
                phraseRepository.findWithLikeCountsOrderByIssuedAtDesc(PageRequest.of(0, 10));
        return toResponses(rows, currentUserId);
    }

    public List<PhraseResponse> allSanctions(Long currentUserId) {
        List<PhraseWithLikeCount> rows = phraseRepository.findAllWithLikeCountsOrderByLikeCountDesc();
        return toResponses(rows, currentUserId);
    }

    public PhraseLikeResponse toggleLike(Long phraseId, Long userId) {
        Phrase phrase = phraseRepository.findById(phraseId)
                .orElseThrow(() -> new ResourceNotFoundException("Phrase not found: " + phraseId));

        PhraseUser user = phraseUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isAdmin()) {
            throw new AdminCannotLikeException("Admins cannot like phrases");
        }

        boolean nowLiked = phraseLikeRepository.findByPhraseIdAndUserId(phraseId, userId)
                .map(existing -> {
                    phraseLikeRepository.delete(existing);
                    return false;
                })
                .orElseGet(() -> {
                    PhraseLike like = new PhraseLike();
                    like.setPhrase(phrase);
                    like.setUser(user);
                    phraseLikeRepository.save(like);
                    return true;
                });

        long likeCount = phraseLikeRepository.countByPhraseId(phraseId);
        return new PhraseLikeResponse(phraseId, likeCount, nowLiked);
    }

    private List<PhraseResponse> toResponses(List<PhraseWithLikeCount> rows, Long currentUserId) {
        Set<Long> likedPhraseIds = currentUserId != null
                ? new HashSet<>(phraseLikeRepository.findPhraseIdsByUserId(currentUserId))
                : Set.of();

        return rows.stream()
                .map(row -> toResponse(row.getPhrase(), row.getLikeCount(), likedPhraseIds.contains(row.getPhrase().getId())))
                .toList();
    }

    private PhraseResponse toResponse(Phrase phrase, long likeCount, boolean likedByCurrentUser) {
        return new PhraseResponse(
                phrase.getId(),
                phrase.getIssuer().getUsername(),
                phrase.getReceiver().getUsername(),
                phrase.getFineType().getName(),
                phrase.getFineType().getName().getDefaultAmount(),
                phrase.getText(),
                phrase.getIssuedAt(),
                likeCount,
                likedByCurrentUser
        );
    }
}
