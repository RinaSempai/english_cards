package com.example.englishtraining;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class LevelActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ListView listViewWords;
    private int savedScrollPosition = 0;  // Переменная для хранения позиции прокрутки
    private ArrayList<WordItem> words; // Список слов для дальнейшего использования
    private WordAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);

        dbHelper = new DatabaseHelper(this);
        listViewWords = findViewById(R.id.listViewWords);
        Button buttonMarkUnknown = findViewById(R.id.buttonMarkUnknown);
        Button buttonMarkKnown = findViewById(R.id.buttonMarkKnown);

        int levelId = getIntent().getIntExtra("LEVEL_ID", -1);
        loadWords(levelId);

        buttonMarkUnknown.setOnClickListener(v -> markWordsAsUnknown());
        buttonMarkKnown.setOnClickListener(v -> markWordsAsKnown());

        if (savedInstanceState != null) {
            savedScrollPosition = savedInstanceState.getInt("scroll_position", 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        int levelId = getIntent().getIntExtra("LEVEL_ID", -1);
        loadWords(levelId); // Обновляем список слов

        // Восстанавливаем позицию списка
        listViewWords.post(() -> listViewWords.setSelection(savedScrollPosition));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Сохраняем текущую позицию списка
        savedScrollPosition = listViewWords.getFirstVisiblePosition();
        outState.putInt("scroll_position", savedScrollPosition);
    }

    private void loadWords(int levelId) {
        Cursor cursor = dbHelper.getWords(levelId);
        words = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                int wordId = cursor.getInt(cursor.getColumnIndex("id"));
                String word = cursor.getString(cursor.getColumnIndex("word"));
                boolean isKnown = dbHelper.isWordKnown(wordId);
                words.add(new WordItem(wordId, word, isKnown));
            } while (cursor.moveToNext());
        }
        cursor.close();

        adapter = new WordAdapter(this, words, dbHelper);
        listViewWords.setAdapter(adapter);

        // Восстанавливаем позицию списка после загрузки данных
        listViewWords.setSelection(savedScrollPosition);
    }

    private void markWordsAsUnknown() {
        for (WordItem wordItem : words) {
            if (wordItem.isKnown()) {
                dbHelper.updateWordStatus(wordItem.getWordId(), false);
                wordItem.setKnown(false);
            }
        }
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Отмечены как неизвестные", Toast.LENGTH_SHORT).show();
    }

    private void markWordsAsKnown() {
        for (WordItem wordItem : words) {
            if (!wordItem.isKnown()) {
                dbHelper.updateWordStatus(wordItem.getWordId(), true);
                wordItem.setKnown(true);
            }
        }
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Отмечены как известные", Toast.LENGTH_SHORT).show();
    }
}
