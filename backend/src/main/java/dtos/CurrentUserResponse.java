package dtos;

import java.math.BigDecimal;

public record CurrentUserResponse(
        Long id,
        String username,
        boolean admin,
        BigDecimal accountBalance
) {
}
