package com.example.englishcards;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "dictionary.db";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_PATH = "/data/data/com.example.englishcards/databases/";
    private final Context context;
    private SQLiteDatabase database;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // The database is copied from assets, so no creation is needed here
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrade as needed
    }

    public void createDatabase() throws IOException {
        boolean dbExist = checkDatabase();

        if (!dbExist) {
            this.getReadableDatabase();
            try {
                copyDatabase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    private boolean checkDatabase() {
        SQLiteDatabase checkDb = null;
        try {
            String path = DATABASE_PATH + DATABASE_NAME;
            checkDb = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLException e) {
            // Database doesn't exist yet
        }

        if (checkDb != null) {
            checkDb.close();
        }

        return checkDb != null;
    }

    private void copyDatabase() throws IOException {
        InputStream input = context.getAssets().open(DATABASE_NAME);
        String outFileName = DATABASE_PATH + DATABASE_NAME;
        OutputStream output = new FileOutputStream(outFileName);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer)) > 0) {
            output.write(buffer, 0, length);
        }

        output.flush();
        output.close();
        input.close();
    }

    public void openDatabase() throws SQLException {
        String path = DATABASE_PATH + DATABASE_NAME;
        database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
    }

    @Override
    public synchronized void close() {
        if (database != null) {
            database.close();
        }
        super.close();
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

    public void addTranslation(int wordId, String translation, String usageExample, String exampleTranslation) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO Translations (word_id, translation, usage_example, example_translation) VALUES (?, ?, ?, ?)",
                new Object[]{wordId, translation, usageExample, exampleTranslation});
        db.close();
    }

    public void updateTranslation(int translationId, String translation, String usageExample, String exampleTranslation) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE Translations SET translation = ?, usage_example = ?, example_translation = ? WHERE id = ?",
                new Object[]{translation, usageExample, exampleTranslation, translationId});
        db.close();
    }

    public void deleteTranslation(int translationId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM Translations WHERE id = ?", new Object[]{translationId});
        db.close();
    }

    public void addWord(int levelId, String word, String usageExample, String exampleTranslation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("level_id", levelId);
        values.put("word", word);
        long wordId = db.insert("Words", null, values);

        if (wordId != -1) {
            ContentValues translationValues = new ContentValues();
            translationValues.put("word_id", wordId);
            translationValues.put("translation", word);
            translationValues.put("usage_example", usageExample);
            translationValues.put("example_translation", exampleTranslation);
            db.insert("Translations", null, translationValues);
        }

        db.close();
    }
}
