package com.example.englishcards;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.database.SQLException;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "dictionary.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Database creation is handled by the existing dictionary.db
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrade as needed
    }

    public Cursor getLevels() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM Levels", null);
    }

    public Cursor getWords(int levelId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM Words WHERE level_id = ?", new String[]{String.valueOf(levelId)});
    }

    public Cursor getTranslations(int wordId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT Words.word, Translations.translation, Translations.usage_example, Translations.example_translation FROM Words LEFT JOIN Translations ON Words.id = Translations.word_id WHERE Words.id = ?", new String[]{String.valueOf(wordId)});
    }
}
