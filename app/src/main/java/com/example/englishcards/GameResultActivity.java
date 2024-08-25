package com.example.englishcards;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GameResultActivity extends AppCompatActivity {

    private TextView resultTextView;
    private Button finishButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        resultTextView = findViewById(R.id.resultTextView);
        finishButton = findViewById(R.id.finishButton);

        long timeElapsed = getIntent().getLongExtra("TIME_ELAPSED", 0);
        int seconds = (int) (timeElapsed / 1000);
        resultTextView.setText(String.format("Time: %02d:%02d", seconds / 60, seconds % 60));

        finishButton.setOnClickListener(v -> {
            Intent intent = new Intent(GameResultActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Optionally close this activity
        });
    }
}
