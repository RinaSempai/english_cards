package com.example.englishtraining;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultsActivity extends AppCompatActivity {

    private TextView textViewResultSummary;
    private Button buttonFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        textViewResultSummary = findViewById(R.id.textViewResultSummary);
        buttonFinish = findViewById(R.id.buttonFinish);

        Intent intent = getIntent();
        String resultSummary = intent.getStringExtra("RESULT_SUMMARY");

        textViewResultSummary.setText(resultSummary);

        buttonFinish.setOnClickListener(v -> finish());
    }
}