package dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import models.FineType;

public record SanctionRequest(
    @NotBlank String username,
    @NotNull FineType.Name type,
    String text
) {
}

