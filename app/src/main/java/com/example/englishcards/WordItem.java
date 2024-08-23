package com.example.englishcards;

public class WordItem {
    private int wordId;
    private String word;
    private boolean isKnown;

    public WordItem(int wordId, String word, boolean isKnown) {
        this.wordId = wordId;
        this.word = word;
        this.isKnown = isKnown;
    }

    public int getWordId() {
        return wordId;
    }

    public String getWord() {
        return word;
    }

    public boolean isKnown() {
        return isKnown;
    }

    public void setKnown(boolean known) {
        isKnown = known;
    }
}
