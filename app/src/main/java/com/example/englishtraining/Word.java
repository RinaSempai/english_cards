package com.example.englishtraining;

public class Word {
    private int id;
    private String word;
    private String translation;
    private String usageExample;
    private String exampleTranslation;
    private byte[] audio; // Добавлен массив байтов для хранения аудио

    public Word(int id, String word, String translation, String usageExample, String exampleTranslation, byte[] audio) {
        this.id = id;
        this.word = word;
        this.translation = translation;
        this.usageExample = usageExample;
        this.exampleTranslation = exampleTranslation;
        this.audio = audio; // Инициализация аудио
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

    public byte[] getAudio() { // Метод для получения аудио
        return audio;
    }
}
