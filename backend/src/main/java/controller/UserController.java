package controller;

import dtos.AccountResetRequest;
import dtos.AccountResetResponse;
import dtos.CreateUserRequest;
import dtos.CurrentUserResponse;
import dtos.UserSummary;
import exceptions.ResourceNotFoundException;
import jakarta.validation.Valid;
import models.PhraseUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/me")
    public CurrentUserResponse me(@AuthenticationPrincipal PhraseUserPrincipal principal) {
        PhraseUser user = phraseUserRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return new CurrentUserResponse(user.getId(), user.getUsername(), user.isAdmin(), user.getAccountBalance());
    }

    @GetMapping
    public List<UserSummary> listUsers() {
        return phraseUserRepository.findAll().stream()
                .filter(user -> !user.isAdmin())
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

    @PostMapping
    public ResponseEntity<UserSummary> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserSummary response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable String username,
                                            @AuthenticationPrincipal PhraseUserPrincipal principal) {
        userService.deleteUser(username, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
