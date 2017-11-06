package com.chrismsolutions.chrismdefinitions.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.chrismsolutions.chrismdefinitions.data.DefinitionsContract.DefinitionsEntry;

/**
 * Created by Christian Myrvold on 29.10.2017.
 */

public class TestDB
{
    private static final String LOG_TAG = "TEST";
    private static final String SQL_JOKER_ID = "=?";

    public static void testDB(Context context)
    {
        DefinitionDBHelper dbHelper = new DefinitionDBHelper(context);
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        //Find all folders
        Log.i(LOG_TAG, "FOLDERS");

        String[] projection = {
                DefinitionsEntry._ID,
                DefinitionsEntry.COLUMN_FOLDER_NAME
        };

        Cursor cursor = database.query(
                DefinitionsEntry.TABLE_NAME_FOLDERS,
                projection,
                null,
                null,
                null,
                null,
                null
                );

        while (cursor.moveToNext())
        {
            Log.i(LOG_TAG, cursor.getString(cursor.getColumnIndexOrThrow(DefinitionsEntry._ID)));
            Log.i(LOG_TAG, cursor.getString(cursor.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_FOLDER_NAME)));
            Log.i(LOG_TAG, "-------------------------------");
        }
        cursor.close();


        //Find all word cards
        Log.i(LOG_TAG, "WORD CARDS");

        String[] projectionWords = {
                DefinitionsEntry._ID,
                DefinitionsEntry.COLUMN_WORD_CARD_NAME,
                DefinitionsEntry.COLUMN_WORD_CARD_TEXT
        };

        Cursor cursorWords = database.query(
                DefinitionsEntry.TABLE_NAME_WORD_CARDS,
                projectionWords,
                null,
                null,
                null,
                null,
                null
        );

        while (cursorWords.moveToNext())
        {
            Log.i(LOG_TAG, cursorWords.getString(cursorWords.getColumnIndexOrThrow(DefinitionsEntry._ID)));
            Log.i(LOG_TAG, cursorWords.getString(cursorWords.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_WORD_CARD_NAME)));
            Log.i(LOG_TAG, cursorWords.getString(cursorWords.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_WORD_CARD_TEXT)));
            Log.i(LOG_TAG, "-------------------------------");
        }
        cursorWords.close();


        //Find all links
        Log.i(LOG_TAG, "LINKS");

        String[] projectionLinks = {
                DefinitionsEntry.COLUMN_LINK_FOLDER_ID,
                DefinitionsEntry.COLUMN_LINK_WORD_CARD_ID
        };

        Cursor cursorLink = database.query(
                DefinitionsEntry.TABLE_NAME_LINK,
                projectionLinks,
                null,
                null,
                null,
                null,
                null
        );

        while (cursorLink.moveToNext())
        {
            Log.i(LOG_TAG, "FolderID: " + cursorLink.getInt(cursorLink.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_LINK_FOLDER_ID)));
            Log.i(LOG_TAG, "WordID: " + cursorLink.getInt(cursorLink.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_LINK_WORD_CARD_ID)));
            Log.i(LOG_TAG, "-------------------------------");
        }
        cursorLink.close();


        //Test the JOIN statement
        Log.i(LOG_TAG, "JOIN RESULT");
        int folderId = 5;
        SQLiteQueryBuilder dbBuilder = new SQLiteQueryBuilder();
        String statement = DefinitionsEntry.TABLE_NAME_WORD_CARDS +
                " LEFT OUTER JOIN " + DefinitionsEntry.TABLE_NAME_LINK + " ON " +
                DefinitionsEntry.TABLE_NAME_WORD_CARDS + "." + DefinitionsEntry._ID + "=" +
                DefinitionsEntry.TABLE_NAME_LINK + "." + DefinitionsEntry.COLUMN_LINK_WORD_CARD_ID;
        dbBuilder.setTables(statement);

        Log.i(LOG_TAG, statement);

        String[] projectionLink = {
                DefinitionsEntry.TABLE_NAME_WORD_CARDS + "." + DefinitionsEntry._ID,
                DefinitionsEntry.TABLE_NAME_WORD_CARDS + "." + DefinitionsEntry.COLUMN_WORD_CARD_NAME,
                DefinitionsEntry.TABLE_NAME_WORD_CARDS + "." + DefinitionsEntry.COLUMN_WORD_CARD_TEXT
        };
        String selection = DefinitionsEntry.TABLE_NAME_LINK + "." + DefinitionsEntry.COLUMN_LINK_FOLDER_ID + SQL_JOKER_ID;
        String[] selectionArgs = new String[]{String.valueOf(folderId)};

        Cursor cursorJoin = dbBuilder.query(
                dbHelper.getReadableDatabase(),
                projectionLink,
                null,
                null,
                null,
                null,
                null);

        while (cursorJoin.moveToNext())
        {
            Log.i(LOG_TAG, cursorJoin.getString(cursorWords.getColumnIndexOrThrow(DefinitionsEntry._ID)));
            Log.i(LOG_TAG, cursorJoin.getString(cursorWords.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_WORD_CARD_NAME)));
            Log.i(LOG_TAG, cursorJoin.getString(cursorWords.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_WORD_CARD_TEXT)));
            Log.i(LOG_TAG, "-------------------------------");
        }
        cursorJoin.close();

        //Search result for name=%Monitors%
        Log.i(LOG_TAG, "SEARCH RESULT");

        String[] projectionWordsSearch = {
                DefinitionsEntry._ID,
                DefinitionsEntry.COLUMN_WORD_CARD_NAME,
                DefinitionsEntry.COLUMN_WORD_CARD_TEXT
        };

        selection = DefinitionsEntry.COLUMN_WORD_CARD_NAME + " LIKE ?";
        selectionArgs = new String[] {"%e%"};

        Cursor cursorWordsSearch = database.query(
                DefinitionsEntry.TABLE_NAME_WORD_CARDS,
                projectionWords,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        while (cursorWordsSearch.moveToNext())
        {
            Log.i(LOG_TAG, cursorWordsSearch.getString(
                    cursorWordsSearch.getColumnIndexOrThrow(DefinitionsEntry._ID)));
            Log.i(LOG_TAG, cursorWordsSearch.getString(
                    cursorWordsSearch.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_WORD_CARD_NAME)));
            Log.i(LOG_TAG, cursorWordsSearch.getString(
                    cursorWordsSearch.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_WORD_CARD_TEXT)));
            Log.i(LOG_TAG, "-------------------------------");
        }
        cursorWordsSearch.close();
    }
}
