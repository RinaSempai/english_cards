package com.example.englishcards;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryGameActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private List<Card> cards;
    private Button firstButton, secondButton;
    private Card firstCard, secondCard;
    private boolean isFlipping;
    private int matchesFound;
    private GridLayout gridLayout;
    private int levelId;
    private TextView timerTextView;
    private long startTime;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_game);

        dbHelper = new DatabaseHelper(this);
        gridLayout = findViewById(R.id.gridLayout);
        timerTextView = findViewById(R.id.timerTextView); // Add this in your XML
        levelId = getIntent().getIntExtra("LEVEL_ID", -1);

        startTime = SystemClock.elapsedRealtime(); // Start the timer

        loadWords();
        setupCards();

        // Setup timer runnable
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsedMillis = SystemClock.elapsedRealtime() - startTime;
                int seconds = (int) (elapsedMillis / 1000);
                timerTextView.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable); // Stop timer when activity is destroyed
    }

    private void loadWords() {
        cards = new ArrayList<>();
        Cursor cursor = dbHelper.getRandomWords(levelId, 9); // Ensure this fetches 9 unique pairs
        if (cursor.moveToFirst()) {
            do {
                String word = cursor.getString(cursor.getColumnIndex("word"));
                String translation = cursor.getString(cursor.getColumnIndex("translation"));
                cards.add(new Card(word, word)); // Use word as identifier
                cards.add(new Card(translation, word)); // Use translation as identifier
            } while (cursor.moveToNext());
        }
        cursor.close();
        Collections.shuffle(cards);
    }

    private void setupCards() {
        int totalCards = cards.size();
        gridLayout.setColumnCount(3);
        gridLayout.setRowCount(6);

        for (int i = 0; i < totalCards; i++) {
            final int index = i;
            Button cardButton = new Button(this);
            cardButton.setText(""); // Initially hide text
            cardButton.setOnClickListener(v -> {
                if (isFlipping) return;
                Card card = cards.get(index);
                cardButton.setText(card.getText());
                if (firstCard == null) {
                    firstCard = card;
                    firstButton = cardButton;
                } else {
                    secondCard = card;
                    secondButton = cardButton;
                    isFlipping = true;
                    checkMatch();
                }
            });

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            cardButton.setLayoutParams(params);
            gridLayout.addView(cardButton);
        }
    }

    private void checkMatch() {
        if (firstCard.getIdentifier().equalsIgnoreCase(secondCard.getIdentifier())) {
            matchesFound++;
            new Handler().postDelayed(() -> {
                firstButton.setVisibility(View.INVISIBLE);
                secondButton.setVisibility(View.INVISIBLE);
                if (matchesFound == cards.size() / 2) {
                    timerHandler.removeCallbacks(timerRunnable);
                    Intent resultIntent = new Intent(MemoryGameActivity.this, GameResultActivity.class);
                    resultIntent.putExtra("TIME_ELAPSED", SystemClock.elapsedRealtime() - startTime);
                    startActivity(resultIntent);
                }
                resetFlipping();
            }, 1000); // Delay to show the second card flip before hiding
        } else {
            // Flip back the cards after a delay
            new Handler().postDelayed(() -> {
                firstButton.setText("");
                secondButton.setText("");
                resetFlipping();
            }, 1000); // Show both cards briefly before hiding
        }
    }

    private void resetFlipping() {
        firstCard = null;
        secondCard = null;
        firstButton = null;
        secondButton = null;
        isFlipping = false;
    }
}
