package repository;

import models.Phrase;

public class PhraseWithLikeCount {

    private final Phrase phrase;
    private final Long likeCount;

    public PhraseWithLikeCount(Phrase phrase, Long likeCount) {
        this.phrase = phrase;
        this.likeCount = likeCount;
    }

    public Phrase getPhrase() {
        return phrase;
    }

    public long getLikeCount() {
        return likeCount == null ? 0 : likeCount;
    }
}
