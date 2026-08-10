package controller;

import dtos.UserSummary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import repository.PhraseUserRepository;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final PhraseUserRepository phraseUserRepository;

    public UserController(PhraseUserRepository phraseUserRepository) {
        this.phraseUserRepository = phraseUserRepository;
    }

    @GetMapping
    public List<UserSummary> listUsers() {
        return phraseUserRepository.findAll().stream()
                .map(user -> new UserSummary(user.getId(), user.getUsername()))
                .sorted(Comparator.comparing(UserSummary::username, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}
