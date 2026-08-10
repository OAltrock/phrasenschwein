package dtos;

import java.math.BigDecimal;

public record AccountResetResponse(
        Long id,
        String username,
        BigDecimal accountBalance
) {
}
