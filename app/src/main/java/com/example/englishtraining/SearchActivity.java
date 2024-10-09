package com.example.englishtraining;

import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private EditText editTextSearchWord;
    private Button buttonSearch;
    private ListView listViewSearchResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        dbHelper = new DatabaseHelper(this);

        editTextSearchWord = findViewById(R.id.editTextSearchWord);
        buttonSearch = findViewById(R.id.buttonSearch);
        listViewSearchResults = findViewById(R.id.listViewSearchResults);

        buttonSearch.setOnClickListener(v -> searchWord());
    }

    private void searchWord() {
        String searchQuery = editTextSearchWord.getText().toString().trim();
        if (TextUtils.isEmpty(searchQuery)) {
            Toast.makeText(this, "Введите слово для поиска", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cursor = dbHelper.searchWords(searchQuery);
        ArrayList<String> searchResults = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                String word = cursor.getString(cursor.getColumnIndex("word"));
                String translation = cursor.getString(cursor.getColumnIndex("translation"));
                String level = cursor.getString(cursor.getColumnIndex("level"));
                String usageExample = cursor.getString(cursor.getColumnIndex("usage_example"));
                String exampleTranslation = cursor.getString(cursor.getColumnIndex("example_translation"));

                searchResults.add("Слово: " + word + "\nПеревод: " + translation + "\nУровень: " + level +
                        "\nПример использования: " + usageExample + "\nПеревод предложения: " + exampleTranslation);
            } while (cursor.moveToNext());
        } else {
            searchResults.add("Результаты не найдены");
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, searchResults);
        listViewSearchResults.setAdapter(adapter);
    }
}
