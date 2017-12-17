package com.chrismsolutions.chrismdefinitions;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.chrismsolutions.chrismdefinitions.data.DefinitionProvider;
import com.chrismsolutions.chrismdefinitions.data.DefinitionsContract.DefinitionsEntry;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Random;

import static com.chrismsolutions.chrismdefinitions.FinishTestActivity.MAX_LAST_RESULTS;

public class WordCardFlipActivity extends AppCompatActivity
{

    private ArrayList<Word> words;
    WordCardArrayAdapter adapter;
    int highestId = 0;
    int correctResultTotal, wrongResultTotal;

    public static final String INTENT_ADAPTER = "INTENT_ADAPTER";
    public static final String INTENT_WORD_DRAW = "INTENT_WORD_DRAW";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_card_flip);

        //RecyclerView recyclerView = (RecyclerView) findViewById(R.id.word_card_flip_recycler_view);
        //We will always know how many cards we will create
        //recyclerView.setHasFixedSize(true);
        ListView listView = (ListView) findViewById(R.id.list_cards);

        //LinearLayoutManager manager = new LinearLayoutManager(WordCardFlipActivity.this);
        //recyclerView.setLayoutManager(manager);

        if (getIntent() != null)// && getIntent().hasExtra(INTENT_WORD_DRAW))
        {
            Intent callerIntent = getIntent();
            Bundle bundle = callerIntent.getExtras();
            ArrayList<Integer> ids = bundle.getIntegerArrayList(INTENT_ADAPTER);

            int wordDraw = callerIntent.getIntExtra(INTENT_WORD_DRAW, 0);

            initializeData(ids, wordDraw);
        }

        //WordCardRecyclerViewAdapter adapter = new WordCardRecyclerViewAdapter(words, WordCardFlipActivity.this);
        //recyclerView.setAdapter(adapter);

        adapter = new WordCardArrayAdapter(WordCardFlipActivity.this, words);
        listView.setAdapter(adapter);
    }

    private void initializeData(ArrayList<Integer> ids, int wordDraw)
    {
        ArrayList<Word> randomWords = new ArrayList<>();
        words = new ArrayList<>();

        Cursor cursor = queryWordCardsFromDB(ids);

        if (cursor != null)
        {
            while (cursor.moveToNext())
            {
                String wordName = cursor.getString(cursor.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_WORD_CARD_NAME));
                String definition = cursor.getString(cursor.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_WORD_CARD_TEXT));
                int correctLast = cursor.getInt(cursor.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_WORD_CARD_CORRECT_LAST));
                int wrongLast = cursor.getInt(cursor.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_WORD_CARD_WRONG_LAST));
                int total = correctLast + wrongLast;
                double percentage = total == 0 ? 0 : ((double) correctLast/(double) total);
                int percentageInt = (int)(percentage * 100);
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DefinitionsEntry._ID));

                Word word = new Word(
                        wordName,
                        definition,
                        percentageInt,
                        id);
                word.setContext(this);
                randomWords.add(word);
            }
        }

        //Draw @wordDraw amount of random words
        while (wordDraw != 0)
        {
            Random random = new Random();
            int randomIndex = random.nextInt(randomWords.size());
            words.add(randomWords.get(randomIndex));
            randomWords.remove(randomIndex);
            wordDraw--;
        }
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

    public void enableFinishButton()
    {
        Button btnFinish = (Button) findViewById(R.id.finish_test);
        ArrayList<Word> wordList = adapter.getWordList();
        int answerCount = 0;

        for (int i = 0; i < wordList.size(); i++)
        {
            Word word = wordList.get(i);
            if (word.wrongPoint == 1 || word.correctPoint == 1)
            {
                answerCount++;
            }
        }

        //If the number of answers are the same as the size of the word list, we can finish
        btnFinish.setEnabled(answerCount == wordList.size());
    }


    public void btnFinishTest(View view)
    {
        ArrayList<Integer> ids = updateResultInDB();

        Intent finishTestIntent = new Intent(WordCardFlipActivity.this, FinishTestActivity.class);
        /*Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(FinishTestActivity.INTENT_RESULT, adapter.getWordList());
        finishTestIntent.putExtra(FinishTestActivity.INTENT_BUNDLE, bundle);*/
        finishTestIntent.putIntegerArrayListExtra(FinishTestActivity.INTENT_RESULT, ids);
        finishTestIntent.putExtra(FinishTestActivity.INTENT_RESULT_CORRECT, correctResultTotal);
        finishTestIntent.putExtra(FinishTestActivity.INTENT_RESULT_WRONG, wrongResultTotal);
        startActivity(finishTestIntent);
    }

    private ArrayList<Integer> updateResultInDB()
    {
        //ArrayList<Word> words = adapter.getWordList();
        ArrayList<Integer> ids = new ArrayList<>();

        for (int i = 0; i < words.size(); i++)
        {
            Word word = words.get(i);
            Cursor cursorWord = getWordFromDB(word.dbID);

            if (cursorWord.moveToFirst())
            {
                int correctTotal = cursorWord.getInt(cursorWord.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_WORD_CARD_CORRECT_TOTAL));
                int correctLast = cursorWord.getInt(cursorWord.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_WORD_CARD_CORRECT_LAST));
                int wrongTotal = cursorWord.getInt(cursorWord.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_WORD_CARD_WRONG_TOTAL));
                int wrongLast = cursorWord.getInt(cursorWord.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_WORD_CARD_WRONG_LAST));
                int totalLast = correctLast + wrongLast;

                if (word.correctPoint == 1)
                {
                    correctResultTotal++;
                    correctTotal++;

                    if (totalLast == MAX_LAST_RESULTS)
                    {
                        if (correctLast < MAX_LAST_RESULTS)
                        {
                            correctLast++;
                            wrongLast--;
                        }
                    }
                    else
                    {
                        correctLast++;
                    }
                }
                else
                {
                    wrongResultTotal++;
                    wrongTotal++;

                    if (totalLast == MAX_LAST_RESULTS)
                    {
                        if (wrongLast < MAX_LAST_RESULTS)
                        {
                            wrongLast++;
                            correctLast--;
                        }
                    }
                    else
                    {
                        wrongLast++;
                    }
                }

                ContentValues values = new ContentValues();
                values.put(DefinitionsEntry.COLUMN_WORD_CARD_CORRECT_TOTAL, correctTotal);
                values.put(DefinitionsEntry.COLUMN_WORD_CARD_CORRECT_LAST, correctLast);
                values.put(DefinitionsEntry.COLUMN_WORD_CARD_WRONG_TOTAL, wrongTotal);
                values.put(DefinitionsEntry.COLUMN_WORD_CARD_WRONG_LAST, wrongLast);


                String selection = DefinitionsEntry._ID + DefinitionProvider.getSqlJokerId();
                String[] selectionArgs = {String.valueOf(word.dbID)};
                getContentResolver().update(
                        DefinitionsEntry.CONTENT_URI_WORD_CARD,
                        values,
                        selection,
                        selectionArgs);

                ids.add(word.dbID);
            }
        }
        return ids;
    }

    private Cursor getWordFromDB(int dbID)
    {
        String[] projection = new String[]{
                DefinitionsEntry.COLUMN_WORD_CARD_CORRECT_TOTAL,
                DefinitionsEntry.COLUMN_WORD_CARD_CORRECT_LAST,
                DefinitionsEntry.COLUMN_WORD_CARD_WRONG_TOTAL,
                DefinitionsEntry.COLUMN_WORD_CARD_WRONG_LAST
        };

        String selection = DefinitionsEntry.TABLE_NAME_WORD_CARDS + "."
                + DefinitionsEntry._ID + DefinitionProvider.getSqlJokerId();
        String[] selectionArgs = new String[]{String.valueOf(dbID)};

        return getContentResolver().query(
                DefinitionsEntry.CONTENT_URI_WORD_CARD,
                projection,
                selection,
                selectionArgs,
                null
        );
    }

}
