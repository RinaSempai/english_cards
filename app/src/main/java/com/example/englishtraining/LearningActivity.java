package com.example.englishtraining;

import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LearningActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView textViewTimer, textViewQuestionsCount, textViewFeedback, textViewWordTranslation;
    private EditText editTextUserAnswer;
    private Button buttonCheckAnswer, buttonNextWord;
    private ImageButton buttonPlayAudio; // Кнопка для воспроизведения озвучки
    private long startTime, elapsedTime;
    private Handler timerHandler;
    private Runnable timerRunnable;
    private List<Word> wordsList;
    private int currentWordIndex;
    private int correctAnswers, incorrectAnswers;
    private MediaPlayer mediaPlayer; // Поле для MediaPlayer


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
        buttonPlayAudio = findViewById(R.id.buttonPlayAudio); // Инициализация кнопки воспроизведения озвучки

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
        buttonPlayAudio.setOnClickListener(v -> playAudio()); // Обработка нажатия кнопки воспроизведения озвучки

        startLearning();
    }

    private void loadWords(int levelId, String wordsType, int numberOfWords) {
        Cursor cursor;
        if ("Unknown Words".equals(wordsType)) {
            cursor = dbHelper.getUnknownWords(levelId); // Получаем только неизвестные слова
        } else {
            cursor = dbHelper.getWords(levelId); // Получаем все слова
        }

        wordsList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int wordId = cursor.getInt(cursor.getColumnIndex("id"));
                String word = cursor.getString(cursor.getColumnIndex("word"));
                byte[] audio = cursor.getBlob(cursor.getColumnIndex("audio")); // Извлечение аудио

                // Load translation and example
                Cursor transCursor = dbHelper.getWordTranslations(wordId);
                if (transCursor.moveToFirst()) {
                    String translation = transCursor.getString(transCursor.getColumnIndex("translation"));
                    String usageExample = transCursor.getString(transCursor.getColumnIndex("usage_example"));
                    String exampleTranslation = transCursor.getString(transCursor.getColumnIndex("example_translation"));
                    wordsList.add(new Word(wordId, word, translation, usageExample, exampleTranslation, audio));
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

        // Остановить воспроизведение, если оно идет
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release(); // Освободить ресурсы
            mediaPlayer = null; // Сбросить ссылку
            isPlaying = false; // Установить состояние воспроизведения в false
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

        textViewQuestionsCount.setText(String.format("Вопросы: %d/%d", currentWordIndex + 1, wordsList.size()));

        // Re-enable Check Answer button and EditText
        buttonCheckAnswer.setEnabled(true);
        editTextUserAnswer.setEnabled(true);

        // Hide Play Audio button and Next Word button initially
        buttonPlayAudio.setVisibility(View.INVISIBLE);
        buttonNextWord.setVisibility(View.INVISIBLE);
    }

    private void checkAnswer() {
        if (currentWordIndex < 0 || currentWordIndex >= wordsList.size()) {
            return;
        }

        Word currentWord = wordsList.get(currentWordIndex);
        String userAnswer = editTextUserAnswer.getText().toString().trim();

        if (userAnswer.equalsIgnoreCase(currentWord.getWord().trim())) {
            textViewFeedback.setText("Верно! " + currentWord.getUsageExample());
            correctAnswers++;
            // Mark the word as known in the database
            dbHelper.updateWordStatus(currentWord.getId(), true);
        } else {
            textViewFeedback.setText("Неверно! Правильный ответ: " + currentWord.getWord());
            textViewWordTranslation.setText(currentWord.getTranslation());
            incorrectAnswers++;
        }

        buttonCheckAnswer.setEnabled(false); // Disable check answer button
        editTextUserAnswer.setEnabled(false); // Disable input field
        buttonNextWord.setVisibility(View.VISIBLE); // Show next word button

        // Show Play Audio button after checking the answer
        buttonPlayAudio.setVisibility(View.VISIBLE);
    }

    private boolean isPlaying = false; // Переменная для отслеживания состояния воспроизведения

    private void playAudio() {
        if (currentWordIndex < 0 || currentWordIndex >= wordsList.size() || isPlaying) {
            return; // Если уже идет воспроизведение, ничего не делаем
        }

        isPlaying = true; // Устанавливаем состояние воспроизведения в true
        Word currentWord = wordsList.get(currentWordIndex);
        byte[] audioData = currentWord.getAudio(); // Получаем аудиоданные

        if (audioData != null) {
            try {
                // Освобождаем ресурсы предыдущего MediaPlayer, если он существует
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                }
                mediaPlayer = new MediaPlayer();

                // Создаем временный файл для воспроизведения аудио
                File tempFile = File.createTempFile("temp_audio", ".wav", getCacheDir());
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    fos.write(audioData);
                }

                // Устанавливаем источник данных из временного файла
                mediaPlayer.setDataSource(tempFile.getAbsolutePath());
                mediaPlayer.prepare();
                mediaPlayer.start();

                // Освобождение ресурсов после завершения воспроизведения
                mediaPlayer.setOnCompletionListener(mp -> {
                    mp.release(); // Освобождаем ресурсы
                    mediaPlayer = null; // Сбрасываем ссылку
                    tempFile.delete(); // Удаляем временный файл
                    isPlaying = false; // Устанавливаем состояние воспроизведения в false
                });
            } catch (IOException e) {
                e.printStackTrace(); // Обработка ошибок
                isPlaying = false; // Устанавливаем состояние воспроизведения в false в случае ошибки
            }
        } else {
            isPlaying = false; // Устанавливаем состояние воспроизведения в false, если аудиоданные отсутствуют
        }
    }

    private void endLearning() {
        timerHandler.removeCallbacks(timerRunnable);

        elapsedTime = SystemClock.uptimeMillis() - startTime;
        int seconds = (int) (elapsedTime / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;

        String resultSummary = String.format("Итоговое время: %02d:%02d\nПравильных ответов: %d\nНеверных ответов: %d",
                minutes, seconds, correctAnswers, incorrectAnswers);

        Intent resultIntent = new Intent(LearningActivity.this, ResultsActivity.class);
        resultIntent.putExtra("RESULT_SUMMARY", resultSummary);
        startActivity(resultIntent);

        // Optionally finish the current activity if you don't want to return to it
        finish();
    }
}
