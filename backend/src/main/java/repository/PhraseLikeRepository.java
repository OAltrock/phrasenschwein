package repository;

import models.PhraseLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PhraseLikeRepository extends JpaRepository<PhraseLike, Long> {

    Optional<PhraseLike> findByPhraseIdAndUserId(Long phraseId, Long userId);

    long countByPhraseId(Long phraseId);

    @Query("SELECT l.phrase.id FROM PhraseLike l WHERE l.user.id = :userId")
    List<Long> findPhraseIdsByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM PhraseLike l WHERE l.phrase.issuer.id = :userId OR l.phrase.receiver.id = :userId")
    void deleteByPhraseIssuerIdOrReceiverId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM PhraseLike l WHERE l.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
