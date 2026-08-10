package repository;

import models.Phrase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PhraseRepository extends JpaRepository<Phrase, Long> {

    List<Phrase> findTop10ByOrderByIssuedAtDescIdDesc();

    List<Phrase> findAllByOrderByIssuedAtDescIdDesc();
}
