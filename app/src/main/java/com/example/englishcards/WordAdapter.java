package com.example.englishcards;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

public class WordAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<WordItem> words;
    private DatabaseHelper dbHelper;

    public WordAdapter(Context context, ArrayList<WordItem> words, DatabaseHelper dbHelper) {
        this.context = context;
        this.words = words;
        this.dbHelper = dbHelper;
    }

    @Override
    public int getCount() {
        return words.size();
    }

    @Override
    public Object getItem(int position) {
        return words.get(position);
    }

    @Override
    public long getItemId(int position) {
        return words.get(position).getWordId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_word, parent, false);
        }

        WordItem wordItem = words.get(position);

        TextView textViewWord = convertView.findViewById(R.id.textViewWord);
        CheckBox checkBoxKnown = convertView.findViewById(R.id.checkBoxKnown);

        textViewWord.setText(wordItem.getWord());
        checkBoxKnown.setOnCheckedChangeListener(null);  // Отключаем слушатель, чтобы избежать проблем с повторным использованием View

        checkBoxKnown.setChecked(wordItem.isKnown());

        checkBoxKnown.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dbHelper.updateWordStatus(wordItem.getWordId(), isChecked);
            wordItem.setKnown(isChecked);
        });

        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context, WordDetailActivity.class);
            intent.putExtra("WORD_ID", wordItem.getWordId());
            context.startActivity(intent);
        });

        return convertView;
    }
}
