package controller;

import dtos.AccountResetRequest;
import dtos.AccountResetResponse;
import dtos.UserSummary;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import repository.PhraseUserRepository;
import security.PhraseUserPrincipal;
import service.UserService;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final PhraseUserRepository phraseUserRepository;
    private final UserService userService;

    public UserController(PhraseUserRepository phraseUserRepository, UserService userService) {
        this.phraseUserRepository = phraseUserRepository;
        this.userService = userService;
    }

    @GetMapping
    public List<UserSummary> listUsers() {
        return phraseUserRepository.findAll().stream()
                .map(user -> new UserSummary(user.getId(), user.getUsername()))
                .sorted(Comparator.comparing(UserSummary::username, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @PostMapping("/reset")
    public ResponseEntity<AccountResetResponse> resetAccount(@Valid @RequestBody AccountResetRequest request,
                                                               @AuthenticationPrincipal PhraseUserPrincipal principal) {
        AccountResetResponse response = userService.resetAccountBalance(request.username(), principal.getId());
        return ResponseEntity.ok(response);
    }
}
