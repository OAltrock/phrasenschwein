package dtos;

public record LoginResponse(
        String token,
        String tokenType,
        String username,
        long expiresInMillis
) {
}
