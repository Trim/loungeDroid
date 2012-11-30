package ch.adorsaz.loungeDroid.article;

/**
 * This enum give all possible filter to display articles depending on their
 * state.
 * */
public enum ToDisplay {
    /**
     * Choice of user to display some articles.
     * */
    ALWAYS_PROMPT, ALL, UNREAD, STARRED
}
