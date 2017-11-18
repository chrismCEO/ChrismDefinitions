package com.chrismsolutions.chrismdefinitions.data;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;

import com.chrismsolutions.chrismdefinitions.data.DefinitionsContract.DefinitionsEntry;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Christian Myrvold on 17.11.2017.
 */

public class WordSuggestionDB
{
    public static final String KEY_WORD = SearchManager.SUGGEST_COLUMN_TEXT_1;
    public static final String KEY_DEFINITION = SearchManager.SUGGEST_COLUMN_TEXT_2;

    private static final String DB_NAME = "suggestions";
    private static final String FTS_VIRTUAL_TABLE = "FTSsuggestions";
    private static final int DB_VERSION = 3;

    private final WordSuggestionDBHelper mDBHelper;
    private static final HashMap<String, String> mColumnMap = buildColumnMap();

    public WordSuggestionDB(Context context)
    {
        mDBHelper = new WordSuggestionDBHelper(context);
    }

    private static HashMap<String,String> buildColumnMap()
    {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(KEY_WORD, KEY_WORD);
        map.put(KEY_DEFINITION, KEY_DEFINITION);
        map.put(BaseColumns._ID, "rowid AS " + BaseColumns._ID);

        /*map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        map.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);*/

        return map;
    }

    public Cursor getWord(String rowId, String[] projection)
    {
        String selection = "rowid =?";
        String[] selectionArgs = new String[]{rowId};

        return query(selection, selectionArgs, projection);
    }

    public Cursor getWordMatches(String query, String[] projection)
    {
       String selection = KEY_WORD + " MATCH ?";
       String[] selectionArgs = new String[]{query+"*"};

       return query(selection, selectionArgs, projection);
    }

    private Cursor query(String selection, String[] selectionArgs, String[] projection)
    {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(FTS_VIRTUAL_TABLE);
        queryBuilder.setProjectionMap(mColumnMap);

        Cursor cursor = queryBuilder.query(
                mDBHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null);

        if (cursor != null && !cursor.moveToFirst())
        {
            cursor.close();
            cursor = null;
        }
        return cursor;
    }

    private static class WordSuggestionDBHelper extends SQLiteOpenHelper
    {
        private final Context mContext;
        private SQLiteDatabase mDatabase;

        private static final String FTS_TABLE_CREATE =
                "CREATE VIRTUAL TABLE IF NOT EXISTS " + FTS_VIRTUAL_TABLE +
                        " USING fts4 (" +
                        KEY_WORD + ", " +
                        KEY_DEFINITION + ");";

        public WordSuggestionDBHelper(Context context)
        {
            super(context, DB_NAME, null, DB_VERSION);
            mContext = context;
            mDatabase = getWritableDatabase();
            onCreate(mDatabase);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            mDatabase = db;
            mDatabase.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
            mDatabase.execSQL(FTS_TABLE_CREATE);
            loadDictionary();
        }

        private void loadDictionary()
        {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try
                    {
                        loadWords();
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }

        private void loadWords() throws IOException
        {
            final Resources resources = mContext.getResources();
            Cursor cursor = queryWordCards(mContext);

            if (cursor != null)
            {
                try
                {
                    String line;
                    while (cursor.moveToNext())
                    {
                        String name = cursor.getString(
                                cursor.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_WORD_CARD_NAME));
                        String definition = cursor.getString(cursor.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_WORD_CARD_TEXT));
                        long id = addWord(name, definition);
                    }
                }
                finally {
                    cursor.close();
                }
            }
        }

        private long addWord(String name, String definition) {
            ContentValues initialValues = new ContentValues();
            initialValues.put(KEY_WORD, name);
            initialValues.put(KEY_DEFINITION, definition);

            return mDatabase.insert(FTS_VIRTUAL_TABLE, null, initialValues);
        }

        private Cursor queryWordCards(Context context)
        {
            String[] projection = {
                    DefinitionsContract.DefinitionsEntry._ID,
                    DefinitionsContract.DefinitionsEntry.COLUMN_WORD_CARD_NAME,
                    DefinitionsContract.DefinitionsEntry.COLUMN_WORD_CARD_TEXT
            };

            Cursor cursor = null;
            cursor = context.getContentResolver().query(
                    DefinitionsContract.DefinitionsEntry.CONTENT_URI_WORD_CARD,
                    projection,
                    null,
                    null,
                    null);

            return cursor;
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
            onCreate(db);
        }
    }
}
