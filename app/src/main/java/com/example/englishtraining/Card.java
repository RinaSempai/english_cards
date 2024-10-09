package com.example.englishtraining;

public class Card {
    private String text;
    private String identifier;

    public Card(String text, String identifier) {
        this.text = text;
        this.identifier = identifier;
    }

    public String getText() {
        return text;
    }

    public String getIdentifier() {
        return identifier;
    }
}
