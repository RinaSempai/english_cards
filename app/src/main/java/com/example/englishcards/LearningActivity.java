package com.example.englishcards;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LearningActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView textViewTimer, textViewQuestionsCount, textViewFeedback, textViewWordTranslation;
    private EditText editTextUserAnswer;
    private Button buttonCheckAnswer, buttonNextWord;
    private long startTime, elapsedTime;
    private Handler timerHandler;
    private Runnable timerRunnable;
    private List<Word> wordsList;
    private int currentWordIndex;
    private int correctAnswers, incorrectAnswers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning);

        dbHelper = new DatabaseHelper(this);

        textViewTimer = findViewById(R.id.textViewTimer);
        textViewQuestionsCount = findViewById(R.id.textViewQuestionsCount);
        textViewFeedback = findViewById(R.id.textViewFeedback);
        textViewWordTranslation = findViewById(R.id.textViewWordTranslation);
        editTextUserAnswer = findViewById(R.id.editTextUserAnswer);
        buttonCheckAnswer = findViewById(R.id.buttonCheckAnswer);
        buttonNextWord = findViewById(R.id.buttonNextWord);

        timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long time = SystemClock.uptimeMillis() - startTime;
                int seconds = (int) (time / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                textViewTimer.setText(String.format("%02d:%02d:%02d", minutes, seconds, (int) (time % 1000 / 10)));
                timerHandler.postDelayed(this, 100);
            }
        };

        Intent intent = getIntent();
        int levelId = intent.getIntExtra("LEVEL_ID", -1);
        String wordsType = intent.getStringExtra("WORDS_TYPE");
        int numberOfWords = intent.getIntExtra("NUMBER_OF_WORDS", 10);

        loadWords(levelId, wordsType, numberOfWords);

        buttonCheckAnswer.setOnClickListener(v -> checkAnswer());
        buttonNextWord.setOnClickListener(v -> showNextWord());

        startLearning();
    }

    private void loadWords(int levelId, String wordsType, int numberOfWords) {
        Cursor cursor = dbHelper.getWords(levelId);
        wordsList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int wordId = cursor.getInt(cursor.getColumnIndex("id"));
                String word = cursor.getString(cursor.getColumnIndex("word"));

                // Load translation and example
                Cursor transCursor = dbHelper.getWordTranslations(wordId);
                if (transCursor.moveToFirst()) {
                    String translation = transCursor.getString(transCursor.getColumnIndex("translation"));
                    String usageExample = transCursor.getString(transCursor.getColumnIndex("usage_example"));
                    String exampleTranslation = transCursor.getString(transCursor.getColumnIndex("example_translation"));
                    wordsList.add(new Word(wordId, word, translation, usageExample, exampleTranslation));
                }
                transCursor.close();
            } while (cursor.moveToNext());
        }
        cursor.close();

        // Shuffle and limit words
        Collections.shuffle(wordsList);
        if (wordsList.size() > numberOfWords) {
            wordsList = wordsList.subList(0, numberOfWords);
        }
    }

    private void startLearning() {
        startTime = SystemClock.uptimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);

        correctAnswers = 0;
        incorrectAnswers = 0;
        currentWordIndex = -1;

        showNextWord();
    }

    private void showNextWord() {
        if (wordsList.isEmpty()) {
            endLearning();
            return;
        }

        currentWordIndex++;
        if (currentWordIndex >= wordsList.size()) {
            endLearning();
            return;
        }

        Word currentWord = wordsList.get(currentWordIndex);

        textViewWordTranslation.setText(currentWord.getTranslation());
        editTextUserAnswer.setText("");
        textViewFeedback.setText("");

        textViewQuestionsCount.setText(String.format("Questions: %d/%d", currentWordIndex + 1, wordsList.size()));

        // Re-enable Check Answer button and EditText
        buttonCheckAnswer.setEnabled(true);
        editTextUserAnswer.setEnabled(true);

        // Hide Next Word button
        buttonNextWord.setVisibility(View.INVISIBLE);
    }

    private void checkAnswer() {
        if (currentWordIndex < 0 || currentWordIndex >= wordsList.size()) {
            return;
        }

        Word currentWord = wordsList.get(currentWordIndex);
        String userAnswer = editTextUserAnswer.getText().toString().trim();

        if (userAnswer.equalsIgnoreCase(currentWord.getWord())) {
            textViewFeedback.setText("Correct! " + currentWord.getUsageExample());
            correctAnswers++;
            // Mark the word as known in the database
            dbHelper.updateWordStatus(currentWord.getId(), true);
        } else {
            textViewFeedback.setText("Incorrect! The correct answer is: " + currentWord.getWord());
            textViewWordTranslation.setText(currentWord.getTranslation());
            incorrectAnswers++;
        }

        buttonCheckAnswer.setEnabled(false); // Disable check answer button
        editTextUserAnswer.setEnabled(false); // Disable input field
        buttonNextWord.setVisibility(View.VISIBLE); // Show next word button
    }

    private void endLearning() {
        timerHandler.removeCallbacks(timerRunnable);

        elapsedTime = SystemClock.uptimeMillis() - startTime;
        int seconds = (int) (elapsedTime / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;

        String resultSummary = String.format("Time: %02d:%02d\nCorrect Answers: %d\nIncorrect Answers: %d",
                minutes, seconds, correctAnswers, incorrectAnswers);

        Intent resultIntent = new Intent(LearningActivity.this, ResultsActivity.class);
        resultIntent.putExtra("RESULT_SUMMARY", resultSummary);
        startActivity(resultIntent);

        // Optionally finish the current activity if you don't want to return to it
        finish();
    }
}
