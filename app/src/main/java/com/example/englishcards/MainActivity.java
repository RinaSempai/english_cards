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
    private Button buttonStartLearning;
    private Button buttonSearchWord;
    private Button buttonAddLevel;
    private Button buttonDeleteLevel;

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
        buttonStartLearning = findViewById(R.id.buttonStartLearning);

        loadLevels();

        buttonAddWord.setOnClickListener(v -> showAddWordDialog());
        buttonStartLearning.setOnClickListener(v -> showStartLearningDialog());

        buttonSearchWord = findViewById(R.id.buttonSearchWord);

        buttonSearchWord.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        buttonAddLevel = findViewById(R.id.buttonAddLevel);

        buttonAddLevel.setOnClickListener(v -> showAddLevelDialog());

        buttonDeleteLevel = findViewById(R.id.buttonDeleteLevel);
        buttonDeleteLevel.setOnClickListener(v -> showDeleteLevelDialog());

        Button buttonMemoryGame = findViewById(R.id.buttonMemoryGame);
        buttonMemoryGame.setOnClickListener(v -> showSelectLevelForMemoryGameDialog());
    }

    private void showSelectLevelForMemoryGameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Level for Memory Game");

        // Get levels from the database
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

        // Create an adapter for the levels
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, levels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final Spinner spinnerLevels = new Spinner(this);
        spinnerLevels.setAdapter(adapter);

        builder.setView(spinnerLevels);

        builder.setPositiveButton("Start", (dialog, which) -> {
            int selectedLevelPosition = spinnerLevels.getSelectedItemPosition();
            int selectedLevelId = levelIds.get(selectedLevelPosition);

            Intent intent = new Intent(MainActivity.this, MemoryGameActivity.class);
            intent.putExtra("LEVEL_ID", selectedLevelId);
            startActivity(intent);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDeleteLevelDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Level");

        // Get levels from the database
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

        // Create an adapter for the levels
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, levels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final Spinner spinnerLevels = new Spinner(this);
        spinnerLevels.setAdapter(adapter);

        builder.setView(spinnerLevels);

        builder.setPositiveButton("Delete", (dialog, which) -> {
            int selectedLevelPosition = spinnerLevels.getSelectedItemPosition();
            int selectedLevelId = levelIds.get(selectedLevelPosition);

            dbHelper.deleteLevel(selectedLevelId);
            Toast.makeText(MainActivity.this, "Level deleted", Toast.LENGTH_SHORT).show();
            loadLevels();  // Refresh the levels list after deletion
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showAddLevelDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Level");

        final EditText input = new EditText(this);
        input.setHint("Enter level name");
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String levelName = input.getText().toString().trim();
            if (levelName.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter a level name", Toast.LENGTH_SHORT).show();
            } else {
                dbHelper.addLevel(levelName);
                Toast.makeText(MainActivity.this, "Level added successfully", Toast.LENGTH_SHORT).show();
                loadLevels();  // Refresh the list of levels
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
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

            if (word.isEmpty() || translation.isEmpty() || usageExample.isEmpty() || exampleTranslation.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                dbHelper.addWord(selectedLevelId, word, translation, usageExample, exampleTranslation);
                Toast.makeText(MainActivity.this, "Word added successfully", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showStartLearningDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_start_learning, null);
        builder.setView(dialogView);

        Spinner spinnerLevel = dialogView.findViewById(R.id.spinnerLevel);
        Spinner spinnerWordsType = dialogView.findViewById(R.id.spinnerWordsType);
        Spinner spinnerNumberOfWords = dialogView.findViewById(R.id.spinnerNumberOfWords);

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

        ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, levels);
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLevel.setAdapter(levelAdapter);

        ArrayAdapter<String> wordsTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"All Words", "Unknown Words"});
        wordsTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWordsType.setAdapter(wordsTypeAdapter);

        ArrayAdapter<String> numberOfWordsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"10", "20", "30", "50"});
        numberOfWordsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNumberOfWords.setAdapter(numberOfWordsAdapter);

        builder.setPositiveButton("Start", (dialog, which) -> {
            int selectedLevelPosition = spinnerLevel.getSelectedItemPosition();
            int selectedLevelId = levelIds.get(selectedLevelPosition);
            String wordsType = spinnerWordsType.getSelectedItem().toString();
            int numberOfWords = Integer.parseInt(spinnerNumberOfWords.getSelectedItem().toString());

            Intent intent = new Intent(MainActivity.this, LearningActivity.class);
            intent.putExtra("LEVEL_ID", selectedLevelId);
            intent.putExtra("WORDS_TYPE", wordsType);
            intent.putExtra("NUMBER_OF_WORDS", numberOfWords);
            startActivity(intent);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
