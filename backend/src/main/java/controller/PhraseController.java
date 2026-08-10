package controller;

import dtos.PhraseResponse;
import dtos.SanctionRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import security.PhraseUserPrincipal;
import service.PhraseService;

import java.util.List;

@RestController
@RequestMapping("/api/phrase")
public class PhraseController {
    private final PhraseService phraseService;

    public PhraseController(PhraseService phraseService) {
        this.phraseService = phraseService;
    }

    @PostMapping("/sanction")
    public ResponseEntity<PhraseResponse> sanction(@Valid @RequestBody SanctionRequest sanctionRequest,
                                                     @AuthenticationPrincipal PhraseUserPrincipal principal) {
        PhraseResponse response = phraseService.sanction(sanctionRequest, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/recent")
    public List<PhraseResponse> recent() {
        return phraseService.recentSanctions();
    }

    @GetMapping("/all")
    public List<PhraseResponse> all() {
        return phraseService.allSanctions();
    }
}
