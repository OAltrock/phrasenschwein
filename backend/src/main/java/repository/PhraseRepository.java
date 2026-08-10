package repository;

import models.Phrase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PhraseRepository extends JpaRepository<Phrase, Long> {

    List<Phrase> findTop10ByOrderByIssuedAtDescIdDesc();

    List<Phrase> findAllByOrderByIssuedAtDescIdDesc();

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Phrase p WHERE p.issuer.id = :userId OR p.receiver.id = :userId")
    void deleteByIssuerIdOrReceiverId(@Param("userId") Long userId);
}
