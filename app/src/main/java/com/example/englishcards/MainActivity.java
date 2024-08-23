package com.example.englishcards;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ListView listViewLevels;
    private Button buttonAddWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        try {
            dbHelper.createDatabase();
            dbHelper.openDatabase();
        } catch (IOException e) {
            throw new Error("Unable to create database");
        }

        listViewLevels = findViewById(R.id.listViewLevels);
        buttonAddWord = findViewById(R.id.buttonAddWord);
        loadLevels();

        buttonAddWord.setOnClickListener(v -> showAddWordDialog());
    }

    private void loadLevels() {
        Cursor cursor = dbHelper.getLevels();
        ArrayList<String> levels = new ArrayList<>();
        ArrayList<Integer> levelIds = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                levels.add(cursor.getString(cursor.getColumnIndex("name")));
                levelIds.add(cursor.getInt(cursor.getColumnIndex("id")));
            } while (cursor.moveToNext());
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, levels);
        listViewLevels.setAdapter(adapter);

        listViewLevels.setOnItemClickListener((parent, view, position, id) -> {
            int levelId = levelIds.get(position);
            Intent intent = new Intent(MainActivity.this, LevelActivity.class);
            intent.putExtra("LEVEL_ID", levelId);
            startActivity(intent);
        });
    }

    private void showAddWordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_word, null);
        builder.setView(dialogView);

        Spinner spinnerLevel = dialogView.findViewById(R.id.spinnerLevels);
        EditText editTextWord = dialogView.findViewById(R.id.editTextWord);
        EditText editTextTranslation = dialogView.findViewById(R.id.editTextTranslation);
        EditText editTextUsageExample = dialogView.findViewById(R.id.editTextExample);
        EditText editTextExampleTranslation = dialogView.findViewById(R.id.editTextExampleTranslation);

        // Load levels into spinner
        Cursor cursor = dbHelper.getLevels();
        ArrayList<String> levels = new ArrayList<>();
        ArrayList<Integer> levelIds = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                levels.add(cursor.getString(cursor.getColumnIndex("name")));
                levelIds.add(cursor.getInt(cursor.getColumnIndex("id")));
            } while (cursor.moveToNext());
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, levels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLevel.setAdapter(adapter);

        builder.setPositiveButton("Save", (dialog, which) -> {
            int selectedLevelPosition = spinnerLevel.getSelectedItemPosition();
            int selectedLevelId = levelIds.get(selectedLevelPosition);
            String word = editTextWord.getText().toString();
            String translation = editTextTranslation.getText().toString();
            String usageExample = editTextUsageExample.getText().toString();
            String exampleTranslation = editTextExampleTranslation.getText().toString();

            if (word.isEmpty() || translation.isEmpty()|| usageExample.isEmpty() || exampleTranslation.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                // Save word and translation to database
                dbHelper.addWord(selectedLevelId, word, translation, usageExample, exampleTranslation);
                Toast.makeText(MainActivity.this, "Word added successfully", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
