package com.example.englishtraining;

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
    private static final String DATABASE_PATH = "/data/data/com.example.englishtraining/databases/";
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

    public void updateDatabase() {
        SQLiteDatabase localDb = this.getWritableDatabase();

        // Загрузите новую базу данных (из assets или другого источника) во временную базу данных
        String tempDatabasePath = DATABASE_PATH + "temp_" + DATABASE_NAME;
        try {
            copyDatabaseToTemp(tempDatabasePath);
            SQLiteDatabase tempDb = SQLiteDatabase.openDatabase(tempDatabasePath, null, SQLiteDatabase.OPEN_READONLY);

            // Получение всех новых строк из таблицы Levels
            Cursor newLevelsCursor = tempDb.rawQuery("SELECT * FROM Levels", null);
            while (newLevelsCursor.moveToNext()) {
                int id = newLevelsCursor.getInt(newLevelsCursor.getColumnIndex("id"));
                String name = newLevelsCursor.getString(newLevelsCursor.getColumnIndex("name"));

                // Проверка, существует ли уже уровень
                Cursor existingLevelCursor = localDb.rawQuery("SELECT * FROM Levels WHERE id = ?", new String[]{String.valueOf(id)});
                if (existingLevelCursor.getCount() == 0) {
                    // Если уровня нет, добавляем его
                    ContentValues values = new ContentValues();
                    values.put("id", id);
                    values.put("name", name);
                    localDb.insert("Levels", null, values);
                }
                existingLevelCursor.close();
            }
            newLevelsCursor.close();

            // Обновление других таблиц
            updateTableFromTemp(localDb, tempDb, "Words");
            updateTableFromTemp(localDb, tempDb, "Translations");
            updateTableFromTemp(localDb, tempDb, "UserWordStatus");

            // Удаление записей из старой базы данных, которых нет в новой
            deleteMissingRecords(localDb, tempDb, "Levels", "id");
            deleteMissingRecords(localDb, tempDb, "Words", "id");
            deleteMissingRecords(localDb, tempDb, "Translations", "id");
            deleteMissingRecords(localDb, tempDb, "UserWordStatus", "word_id");

            // Закрытие временной базы данных
            tempDb.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteMissingRecords(SQLiteDatabase localDb, SQLiteDatabase tempDb, String tableName, String idColumnName) {
        // Получаем все id из временной базы данных
        Cursor tempCursor = tempDb.rawQuery("SELECT " + idColumnName + " FROM " + tableName, null);
        StringBuilder idsInTemp = new StringBuilder();

        while (tempCursor.moveToNext()) {
            int id = tempCursor.getInt(0);
            if (idsInTemp.length() > 0) {
                idsInTemp.append(",");
            }
            idsInTemp.append(id);
        }
        tempCursor.close();

        // Если временная таблица не пустая, удаляем отсутствующие записи
        if (idsInTemp.length() > 0) {
            localDb.execSQL("DELETE FROM " + tableName + " WHERE " + idColumnName + " NOT IN (" + idsInTemp.toString() + ")");
        }
    }

    private void updateTableFromTemp(SQLiteDatabase localDb, SQLiteDatabase tempDb, String tableName) {
        Cursor newCursor = tempDb.rawQuery("SELECT * FROM " + tableName, null);
        while (newCursor.moveToNext()) {
            int id = newCursor.getInt(newCursor.getColumnIndex("id"));

            // Проверка, существует ли уже запись
            Cursor existingCursor = localDb.rawQuery("SELECT * FROM " + tableName + " WHERE id = ?", new String[]{String.valueOf(id)});
            if (existingCursor.getCount() == 0) {
                // Если записи нет, добавляем ее
                ContentValues values = new ContentValues();
                for (int i = 0; i < newCursor.getColumnCount(); i++) {
                    String columnName = newCursor.getColumnName(i);

                    // Проверка типа данных
                    if (newCursor.getType(i) == Cursor.FIELD_TYPE_BLOB) {
                        // Обработка поля типа BLOB
                        byte[] blobData = newCursor.getBlob(i);
                        values.put(columnName, blobData);
                    } else {
                        // Обработка всех остальных типов как строк
                        values.put(columnName, newCursor.getString(i));
                    }
                }
                localDb.insert(tableName, null, values);
            }
            existingCursor.close();
        }
        newCursor.close();
    }



    private void copyDatabaseToTemp(String tempDatabasePath) throws IOException {
        InputStream input = context.getAssets().open(DATABASE_NAME);
        OutputStream output = new FileOutputStream(tempDatabasePath);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer)) > 0) {
            output.write(buffer, 0, length);
        }

        output.flush();
        output.close();
        input.close();
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
        return db.rawQuery("SELECT * FROM Words WHERE level_id = ? ORDER BY lower(word) asc", new String[]{String.valueOf(levelId)});
    }

    public void addWord(int levelId, String word, String translation, String usageExample, String exampleTranslation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("level_id", levelId);
        values.put("word", word);
        long wordId = db.insert("Words", null, values);

        if (wordId != -1) {
            ContentValues translationValues = new ContentValues();
            translationValues.put("word_id", wordId);
            translationValues.put("translation", translation);
            translationValues.put("usage_example", usageExample);
            translationValues.put("example_translation", exampleTranslation);
            db.insert("Translations", null, translationValues);
        }

        db.close();
    }

    public boolean isWordKnown(int wordId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT knows FROM UserWordStatus WHERE word_id = ?", new String[]{String.valueOf(wordId)});
        boolean isKnown = false;
        if (cursor.moveToFirst()) {
            isKnown = cursor.getInt(cursor.getColumnIndex("knows")) == 1;
        }
        cursor.close();
        return isKnown;
    }

    public void updateWordStatus(int wordId, boolean knows) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO UserWordStatus (user_id, word_id, knows) VALUES (?, ?, ?) ON CONFLICT(user_id, word_id) DO UPDATE SET knows = excluded.knows",
                new Object[]{1, wordId, knows ? 1 : 0});
    }


    public Cursor getWordTranslations(int wordId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM Translations WHERE word_id = ?", new String[]{String.valueOf(wordId)});
    }

    public Cursor searchWords(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        query = "%" + query.toLowerCase() + "%";
        return db.rawQuery("SELECT w.word, t.translation, l.name AS level, t.usage_example, t.example_translation " +
                "FROM Words w " +
                "JOIN Translations t ON w.id = t.word_id " +
                "JOIN Levels l ON w.level_id = l.id " +
                "WHERE LOWER(w.word) LIKE ? OR LOWER(t.translation) LIKE ?", new String[]{query, query});
    }

    public void addLevel(String levelName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO Levels (name) VALUES (?)", new Object[]{levelName});
    }

    public void deleteLevel(int levelId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Levels", "id = ?", new String[]{String.valueOf(levelId)});
    }

    public Cursor getRandomWords(int levelId, int limit) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT w.word, t.translation " +
                        "FROM Words w " +
                        "JOIN Translations t ON w.id = t.word_id " +
                        "WHERE w.level_id = ? " +
                        "ORDER BY RANDOM() LIMIT ?",
                new String[]{String.valueOf(levelId), String.valueOf(limit)});
    }

    public Cursor getUnknownWords(int levelId) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Используем параметризованный запрос для безопасности
        String query = "SELECT * FROM Words WHERE level_id = ? AND id NOT IN (SELECT word_id FROM UserWordStatus WHERE knows = 1)";
        return db.rawQuery(query, new String[]{String.valueOf(levelId)});
    }

    public void updateWord(int wordId, String newWord) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE Words SET word = ? WHERE id = ?", new Object[]{newWord, wordId});
        db.close();
    }

}
