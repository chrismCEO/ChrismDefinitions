package com.chrismsolutions.chrismdefinitions.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.chrismsolutions.chrismdefinitions.data.DefinitionsContract.DefinitionsEntry;

/**
 * Created by Christian Myrvold on 27.10.2017.
 */

public class DefinitionDBHelper extends SQLiteOpenHelper
{
    private static final String DB_NAME = "definitions.db";
    private static final int DB_VERSION = 1;

    private static final String SQL_CREATE_FOLDER_TABLE =
            "CREATE TABLE " + DefinitionsEntry.TABLE_NAME_FOLDERS + " ("
            + DefinitionsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + DefinitionsEntry.COLUMN_FOLDER_NAME + " TEXT NOT NULL);";

    private static final String SQL_CREATE_WORD_CARD_TABLE =
            "CREATE TABLE " + DefinitionsEntry.TABLE_NAME_WORD_CARDS + " ("
            + DefinitionsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + DefinitionsEntry.COLUMN_WORD_CARD_NAME + " TEXT NOT NULL, "
            + DefinitionsEntry.COLUMN_WORD_CARD_TEXT + " TEXT NOT NULL);";

    private static final String SQL_CREATE_LINK_TABLE =
            "CREATE TABLE " + DefinitionsEntry.TABLE_NAME_LINK + " ("
            + DefinitionsEntry.COLUMN_LINK_FOLDER_ID + " INT NOT NULL, "
            + DefinitionsEntry.COLUMN_LINK_WORD_CARD_ID + " INT NOT NULL); ";
            /*+ "FOREIGN KEY (" + DefinitionsEntry.COLUMN_LINK_WORD_CARD_ID
                    + ") REFERENCES " +
                    DefinitionsEntry.TABLE_NAME_WORD_CARDS+"("+ DefinitionsEntry._ID+"));";*/

    public DefinitionDBHelper(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(SQL_CREATE_FOLDER_TABLE);
        db.execSQL(SQL_CREATE_WORD_CARD_TABLE);
        db.execSQL(SQL_CREATE_LINK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        //This code will be filled in when new Columns or Tables are added

    }
}
