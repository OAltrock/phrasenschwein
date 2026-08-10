package repository;

import models.PhraseUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface PhraseUserRepository extends JpaRepository<PhraseUser, Long> {

    Optional<PhraseUser> findByUsername(String username);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE PhraseUser u SET u.accountBalance = u.accountBalance + :amount WHERE u.id = :id")
    void addToAccountBalance(@Param("id") Long id, @Param("amount") BigDecimal amount);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE PhraseUser u SET u.accountBalance = 0 WHERE u.id = :id")
    void resetAccountBalance(@Param("id") Long id);
}