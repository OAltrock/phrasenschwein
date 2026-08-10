package dtos;

import jakarta.validation.constraints.NotBlank;

public record AccountResetRequest(
    @NotBlank String username
) {
}
