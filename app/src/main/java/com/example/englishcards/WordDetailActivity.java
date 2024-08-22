package com.example.englishcards;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class WordDetailActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private LinearLayout linearLayoutTranslations;
    private int wordId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_detail);

        dbHelper = new DatabaseHelper(this);

        TextView textViewWord = findViewById(R.id.textViewWord);
        linearLayoutTranslations = findViewById(R.id.linearLayoutTranslations);
        Button buttonAddTranslation = findViewById(R.id.buttonAddTranslation);

        wordId = getIntent().getIntExtra("WORD_ID", -1);

        if (wordId != -1) {
            loadWordDetails();
            loadTranslations();
        } else {
            Toast.makeText(this, "Invalid word ID", Toast.LENGTH_SHORT).show();
            finish();
        }

        buttonAddTranslation.setOnClickListener(v -> showAddTranslationDialog());
    }

    private void loadWordDetails() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT word FROM Words WHERE id = ?", new String[]{String.valueOf(wordId)});
        if (cursor != null && cursor.moveToFirst()) {
            String word = cursor.getString(0); // Index 0 because "word" is the first column in this query
            TextView textViewWord = findViewById(R.id.textViewWord);
            textViewWord.setText(word);
            cursor.close();
        } else {
            Toast.makeText(this, "No word details found", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadTranslations() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT Translations.id, Translations.translation, Translations.usage_example, Translations.example_translation FROM Translations WHERE word_id = ?", new String[]{String.valueOf(wordId)});

        if (cursor != null) {
            linearLayoutTranslations.removeAllViews();
            while (cursor.moveToNext()) {
                int translationId = cursor.getInt(0);
                String translation = cursor.getString(1);
                String usageExample = cursor.getString(2);
                String exampleTranslation = cursor.getString(3);

                View translationView = LayoutInflater.from(this).inflate(R.layout.item_translation, null);
                TextView textViewTranslation = translationView.findViewById(R.id.textViewTranslation);
                TextView textViewUsageExample = translationView.findViewById(R.id.textViewUsageExample);
                TextView textViewExampleTranslation = translationView.findViewById(R.id.textViewExampleTranslation);
                Button buttonEdit = translationView.findViewById(R.id.buttonEdit);
                Button buttonDelete = translationView.findViewById(R.id.buttonDelete);

                textViewTranslation.setText("Translation: " + translation);
                textViewUsageExample.setText("Usage Example: " + usageExample);
                textViewExampleTranslation.setText("Example Translation: " + exampleTranslation);

                buttonEdit.setOnClickListener(v -> showEditTranslationDialog(translationId, translation, usageExample, exampleTranslation));
                buttonDelete.setOnClickListener(v -> showDeleteConfirmationDialog(translationId));

                linearLayoutTranslations.addView(translationView);
            }
            cursor.close();
        }
    }

    private void showAddTranslationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Translation");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_translation, null);
        EditText editTextTranslation = view.findViewById(R.id.editTextTranslation);
        EditText editTextUsageExample = view.findViewById(R.id.editTextUsageExample);
        EditText editTextExampleTranslation = view.findViewById(R.id.editTextExampleTranslation);

        builder.setView(view);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String translation = editTextTranslation.getText().toString();
            String usageExample = editTextUsageExample.getText().toString();
            String exampleTranslation = editTextExampleTranslation.getText().toString();
            addTranslation(translation, usageExample, exampleTranslation);
        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    private void showEditTranslationDialog(int translationId, String currentTranslation, String currentUsageExample, String currentExampleTranslation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Translation");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_translation, null);
        EditText editTextTranslation = view.findViewById(R.id.editTextTranslation);
        EditText editTextUsageExample = view.findViewById(R.id.editTextUsageExample);
        EditText editTextExampleTranslation = view.findViewById(R.id.editTextExampleTranslation);

        editTextTranslation.setText(currentTranslation);
        editTextUsageExample.setText(currentUsageExample);
        editTextExampleTranslation.setText(currentExampleTranslation);

        builder.setView(view);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String translation = editTextTranslation.getText().toString();
            String usageExample = editTextUsageExample.getText().toString();
            String exampleTranslation = editTextExampleTranslation.getText().toString();
            updateTranslation(translationId, translation, usageExample, exampleTranslation);
        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    private void showDeleteConfirmationDialog(int translationId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Translation")
                .setMessage("Are you sure you want to delete this translation?")
                .setPositiveButton("Yes", (dialog, which) -> deleteTranslation(translationId))
                .setNegativeButton("No", null)
                .show();
    }

    private void addTranslation(String translation, String usageExample, String exampleTranslation) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("INSERT INTO Translations (word_id, translation, usage_example, example_translation) VALUES (?, ?, ?, ?)",
                new Object[]{wordId, translation, usageExample, exampleTranslation});
        db.close();
        loadTranslations();
        Toast.makeText(this, "Translation added", Toast.LENGTH_SHORT).show();
    }

    private void updateTranslation(int translationId, String translation, String usageExample, String exampleTranslation) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE Translations SET translation = ?, usage_example = ?, example_translation = ? WHERE id = ?",
                new Object[]{translation, usageExample, exampleTranslation, translationId});
        db.close();
        loadTranslations();
        Toast.makeText(this, "Translation updated", Toast.LENGTH_SHORT).show();
    }

    private void deleteTranslation(int translationId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM Translations WHERE id = ?", new Object[]{translationId});
        db.close();
        loadTranslations();
        Toast.makeText(this, "Translation deleted", Toast.LENGTH_SHORT).show();
    }
}
