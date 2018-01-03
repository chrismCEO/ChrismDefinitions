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
import android.widget.RelativeLayout;

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
    boolean showAds;
    ChrismAdHelper adHelper;

    public static final String INTENT_ADAPTER = "INTENT_ADAPTER";
    public static final String INTENT_WORD_DRAW = "INTENT_WORD_DRAW";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //Ad management
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(MainActivity.IS_PREMIUM_USER))
        {
            showAds = !intent.getBooleanExtra(MainActivity.IS_PREMIUM_USER, true);
        }

        setContentView(R.layout.activity_word_card_flip);

        RelativeLayout relativeLayout = findViewById(R.id.card_flip_container);
        if (showAds)
        {
            adHelper  = new WordCardAdHelper(this, false, false);//WordCardAdHelper.createAdStatic(this, relativeLayout);
            adHelper.createAd(relativeLayout);
        }

        if (getIntent() != null)
        {
            Intent callerIntent = getIntent();
            Bundle bundle = callerIntent.getExtras();
            ArrayList<Integer> ids = bundle.getIntegerArrayList(INTENT_ADAPTER);

            int wordDraw = callerIntent.getIntExtra(INTENT_WORD_DRAW, 0);

            initializeData(ids, wordDraw);
        }

        ListView listView = findViewById(R.id.list_cards);

        adapter = new WordCardArrayAdapter(WordCardFlipActivity.this, words);
        listView.setAdapter(adapter);
    }

    /**
     * Set the data on the card, including word name and definition, and statistics
     * @param ids
     * @param wordDraw
     */
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
                int totalLast = correctLast + wrongLast;
                double percentage = totalLast == 0 ? 0 : ((double) correctLast/(double) totalLast);
                int percentageInt = (int)(percentage * 100);
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DefinitionsEntry._ID));

                Word word = new Word(
                        wordName,
                        definition,
                        percentageInt,
                        id);
                word.setContext(this);
                randomWords.add(word);

                int extraDraws;
                if (totalLast == MAX_LAST_RESULTS)
                {
                    //Only weigh the randomness on words we have tested five or more times
                    extraDraws = getExtraDraws(percentageInt);
                }
                else
                {
                    //Not yet tested this word five or more times, should have high probability to
                    //get drawn
                    extraDraws = MAX_LAST_RESULTS;
                }

                for (; extraDraws > 0; extraDraws--)
                {
                    randomWords.add(word);
                }
            }
        }

        //Draw @wordDraw amount of random words
        while (wordDraw != 0)
        {
            Random random = new Random();
            int randomIndex = random.nextInt(randomWords.size());
            if (words.contains(randomWords.get(randomIndex)))
            {
                randomWords.remove(randomIndex);
                continue;
            }
            words.add(randomWords.get(randomIndex));
            randomWords.remove(randomIndex);
            wordDraw--;
        }
    }

    /**
     * Lower percentage needs to weigh more, so we add that word more times
     * the lower the percentage
     * @param percentageInt
     * @return
     */
    private int getExtraDraws(int percentageInt)
    {
        int extraDraws = 0;

        switch (percentageInt)
        {
            case Word.ZERO:
                extraDraws++;

            case Word.TWENTY_PERCENT:
                extraDraws++;

            case Word.FORTY_PERCENT:
                extraDraws++;

            case Word.SIXTY_PERCENT:
                extraDraws++;

            case Word.EIGHTY_PERCENT:
                extraDraws++;
                break;
        }
        return extraDraws;
    }

    /**
     * Get the random drawn words from the database
     * @param ids
     * @return
     */
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

    /**
     * Only enable the Finish button when all cards have been answered
     */
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

    /**
     * Test is done, go to FinishTestActivity
     * @param view
     */
    public void btnFinishTest(View view)
    {
        ArrayList<Integer> ids = updateResultInDB();

        Intent finishTestIntent = new Intent(WordCardFlipActivity.this, FinishTestActivity.class);
        finishTestIntent.putIntegerArrayListExtra(FinishTestActivity.INTENT_RESULT, ids);
        finishTestIntent.putExtra(FinishTestActivity.INTENT_RESULT_CORRECT, correctResultTotal);
        finishTestIntent.putExtra(FinishTestActivity.INTENT_RESULT_WRONG, wrongResultTotal);
        startActivity(finishTestIntent);
    }

    /**
     * Update the database with the statistics from this test
     * @return
     */
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

    /**
     * Get the specific word from the database
     * @param dbID
     * @return
     */
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
