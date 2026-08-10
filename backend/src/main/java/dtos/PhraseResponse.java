package dtos;

import models.FineType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PhraseResponse(
        Long id,
        String issuer,
        String receiver,
        FineType.Name type,
        BigDecimal amount,
        String text,
        LocalDateTime issuedAt
) {
}
