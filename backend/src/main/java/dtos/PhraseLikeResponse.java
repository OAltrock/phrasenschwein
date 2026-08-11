package dtos;

public record PhraseLikeResponse(
        Long phraseId,
        long likeCount,
        boolean likedByCurrentUser
) {
}
