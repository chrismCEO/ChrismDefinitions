package com.chrismsolutions.chrismdefinitions;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.chrismsolutions.chrismdefinitions.data.DefinitionProvider;
import com.chrismsolutions.chrismdefinitions.data.DefinitionsContract.DefinitionsEntry;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;

import static com.chrismsolutions.chrismdefinitions.Word.EIGHTY_PERCENT;
import static com.chrismsolutions.chrismdefinitions.Word.FORTY_PERCENT;
import static com.chrismsolutions.chrismdefinitions.Word.SIXTY_PERCENT;
import static com.chrismsolutions.chrismdefinitions.Word.TWENTY_PERCENT;

public class FinishTestActivity extends AppCompatActivity
{

    public static final String INTENT_RESULT = "INTENT_RESULT";
    public static final String INTENT_RESULT_CORRECT = "INTENT_RESULT_CORRECT";
    public static final String INTENT_RESULT_WRONG = "INTENT_RESULT_WRONG";


    public static final int MAX_LAST_RESULTS = 5;

    WordCardResultAdapter resultAdapter;
    ArrayList<Word> words;
    ArrayList<Integer> ids = new ArrayList<>();
    int correctResultTotal, wrongResultTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_test);

        Intent intent = getIntent();
        if (intent != null)
        {
            if (intent.hasExtra(INTENT_RESULT))
            {
                ids = intent.getIntegerArrayListExtra(INTENT_RESULT);
                //Bundle bundle = (Bundle) intent.getParcelableExtra(INTENT_BUNDLE);
                //words = (ArrayList<Word>) bundle.getSerializable(INTENT_RESULT);
            }

            if (intent.hasExtra(INTENT_RESULT_CORRECT))
            {
                correctResultTotal = intent.getIntExtra(INTENT_RESULT_CORRECT, 0);
            }

            if (intent.hasExtra(INTENT_RESULT_WRONG))
            {
                wrongResultTotal = intent.getIntExtra(INTENT_RESULT_WRONG, 0);
            }
        }


        TextView result = (TextView)findViewById(R.id.result);
        int totalResult = correctResultTotal + wrongResultTotal;
        double percentageTotal = ((double) correctResultTotal) / ((double)totalResult);
        int percentageTotalInt = (int)(percentageTotal * 100);
        result.setText(getString(R.string.percentage, String.valueOf(percentageTotalInt)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
        {
            Drawable drawable = getResources().getDrawable(setDrawableTopResult(percentageTotalInt));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawable.setTint(getResources().getColor(R.color.colorSecondaryText));
            }
            result.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null,
                    drawable,
                    null,
                    null);
        }

        resultAdapter = new WordCardResultAdapter(FinishTestActivity.this, queryWordCardsFromDB(ids));

        ListView listView = (ListView) findViewById(R.id.list_words_result);
        listView.setAdapter(resultAdapter);
    }

    @Contract(pure = true)
    static public int setDrawableTopResult(int percentage)
    {
        int drawableId;

        if (percentage <= TWENTY_PERCENT)
        {
            drawableId = R.mipmap.ic_sentiment_very_dissatisfied_white_48dp;
        }
        else if (percentage <= FORTY_PERCENT)
        {
            drawableId = R.mipmap.ic_sentiment_dissatisfied_white_48dp;
        }
        else if (percentage <= SIXTY_PERCENT)
        {
            drawableId = R.mipmap.ic_sentiment_neutral_white_48dp;
        }
        else if (percentage <= EIGHTY_PERCENT)
        {
            drawableId = R.mipmap.ic_sentiment_satisfied_white_48dp;
        }
        else
        {
            drawableId = R.mipmap.ic_sentiment_very_satisfied_white_48dp;
        }

        return drawableId;
    }

    private Cursor queryWordCardsFromDB(ArrayList<Integer> ids)
    {
        String[] projection = {
                DefinitionsEntry.TABLE_NAME_WORD_CARDS + "." + DefinitionsEntry._ID,
                DefinitionsEntry.TABLE_NAME_WORD_CARDS + "." + DefinitionsEntry.COLUMN_WORD_CARD_NAME,
                DefinitionsEntry.TABLE_NAME_WORD_CARDS + "." + DefinitionsEntry.COLUMN_WORD_CARD_TEXT,
                DefinitionsEntry.TABLE_NAME_WORD_CARDS + "." + DefinitionsEntry.COLUMN_WORD_CARD_CORRECT_TOTAL,
                DefinitionsEntry.TABLE_NAME_WORD_CARDS + "." + DefinitionsEntry.COLUMN_WORD_CARD_CORRECT_LAST,
                DefinitionsEntry.TABLE_NAME_WORD_CARDS + "." + DefinitionsEntry.COLUMN_WORD_CARD_WRONG_TOTAL,
                DefinitionsEntry.TABLE_NAME_WORD_CARDS + "." + DefinitionsEntry.COLUMN_WORD_CARD_WRONG_LAST
        };

        String[] selectionArgs = new String[ids.size()];

        for (int i = 0; i < ids.size(); i++)
        {
            selectionArgs[i] = String.valueOf(ids.get(i));
        }

        return getContentResolver().query(
                DefinitionsEntry.CONTENT_URI_WORD_CARD,
                projection,
                null,
                selectionArgs,
                null);
    }

    public void btnRestartTest(View view)
    {
        Intent intent = new Intent(FinishTestActivity.this, CreateTestActivity.class);
        startActivity(intent);
    }

    public void btnHome(View view)
    {
        Intent intent = new Intent(FinishTestActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
