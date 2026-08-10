package service;

import dtos.PhraseResponse;
import dtos.SanctionRequest;
import exceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import models.FineType;
import models.Phrase;
import models.PhraseUser;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import repository.FineTypeRepository;
import repository.PhraseRepository;
import repository.PhraseUserRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@Validated
@Transactional
public class PhraseService {

    private final PhraseRepository phraseRepository;
    private final PhraseUserRepository phraseUserRepository;
    private final FineTypeRepository fineTypeRepository;

    public PhraseService(PhraseRepository phraseRepository,
                          PhraseUserRepository phraseUserRepository,
                          FineTypeRepository fineTypeRepository) {
        this.phraseRepository = phraseRepository;
        this.phraseUserRepository = phraseUserRepository;
        this.fineTypeRepository = fineTypeRepository;
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

        return new PhraseResponse(
                phrase.getId(),
                issuer.getUsername(),
                receiver.getUsername(),
                fineType.getName(),
                amount,
                phrase.getText(),
                phrase.getIssuedAt()
        );
    }

    public List<PhraseResponse> recentSanctions() {
        return phraseRepository.findTop10ByOrderByIssuedAtDescIdDesc().stream()
                .map(phrase -> new PhraseResponse(
                        phrase.getId(),
                        phrase.getIssuer().getUsername(),
                        phrase.getReceiver().getUsername(),
                        phrase.getFineType().getName(),
                        phrase.getFineType().getName().getDefaultAmount(),
                        phrase.getText(),
                        phrase.getIssuedAt()
                ))
                .toList();
    }
}
