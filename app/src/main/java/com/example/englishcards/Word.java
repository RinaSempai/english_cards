package com.example.englishcards;

public class Word {
    private int id;
    private String word;
    private String translation;
    private String usageExample;
    private String exampleTranslation;

    public Word(int id, String word, String translation, String usageExample, String exampleTranslation) {
        this.id = id;
        this.word = word;
        this.translation = translation;
        this.usageExample = usageExample;
        this.exampleTranslation = exampleTranslation;
    }

    public int getId() {
        return id;
    }

    public String getWord() {
        return word;
    }

    public String getTranslation() {
        return translation;
    }

    public String getUsageExample() {
        return usageExample;
    }

    public String getExampleTranslation() {
        return exampleTranslation;
    }
}
