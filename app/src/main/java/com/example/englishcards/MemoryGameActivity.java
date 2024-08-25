package com.example.englishcards;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_game);

        dbHelper = new DatabaseHelper(this);
        gridLayout = findViewById(R.id.gridLayout);

        loadWords();
        setupCards();
    }

    private void loadWords() {
        cards = new ArrayList<>();
        Cursor cursor = dbHelper.getRandomWords(15); // Ensure this fetches 15 unique pairs
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
        gridLayout.setColumnCount(5);
        gridLayout.setRowCount(3);

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
                    Toast.makeText(this, "You won!", Toast.LENGTH_SHORT).show();
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
