package repository;

import models.Phrase;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PhraseRepository extends JpaRepository<Phrase, Long> {

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Phrase p WHERE p.issuer.id = :userId OR p.receiver.id = :userId")
    void deleteByIssuerIdOrReceiverId(@Param("userId") Long userId);

    @Query("""
            SELECT new repository.PhraseWithLikeCount(p, COUNT(l))
            FROM Phrase p LEFT JOIN PhraseLike l ON l.phrase = p
            GROUP BY p
            ORDER BY p.issuedAt DESC, p.id DESC
            """)
    List<PhraseWithLikeCount> findWithLikeCountsOrderByIssuedAtDesc(Pageable pageable);

    @Query("""
            SELECT new repository.PhraseWithLikeCount(p, COUNT(l))
            FROM Phrase p LEFT JOIN PhraseLike l ON l.phrase = p
            GROUP BY p
            ORDER BY COUNT(l) DESC, p.issuedAt DESC, p.id DESC
            """)
    List<PhraseWithLikeCount> findAllWithLikeCountsOrderByLikeCountDesc();
}
