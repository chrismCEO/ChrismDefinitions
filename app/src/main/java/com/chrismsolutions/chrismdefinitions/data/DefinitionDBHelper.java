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
    private static final int DB_VERSION = 2;

    private static final String SQL_CREATE_FOLDER_TABLE =
            "CREATE TABLE " + DefinitionsEntry.TABLE_NAME_FOLDERS + " ("
            + DefinitionsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + DefinitionsEntry.COLUMN_FOLDER_NAME + " TEXT NOT NULL);";

    private static final String SQL_CREATE_WORD_CARD_TABLE =
            "CREATE TABLE " + DefinitionsEntry.TABLE_NAME_WORD_CARDS + " ("
            + DefinitionsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + DefinitionsEntry.COLUMN_WORD_CARD_NAME + " TEXT NOT NULL, "
            + DefinitionsEntry.COLUMN_WORD_CARD_TEXT + " TEXT NOT NULL, "
            + DefinitionsEntry.COLUMN_WORD_CARD_CORRECT_TOTAL + " INT DEFAULT 0, "
            + DefinitionsEntry.COLUMN_WORD_CARD_CORRECT_LAST + " INT DEFAULT 0, "
            + DefinitionsEntry.COLUMN_WORD_CARD_WRONG_TOTAL + " INT DEFAULT 0, "
            + DefinitionsEntry.COLUMN_WORD_CARD_WRONG_LAST + " INT DEFAULT 0);";

    private static final String SQL_CREATE_LINK_TABLE =
            "CREATE TABLE " + DefinitionsEntry.TABLE_NAME_LINK + " ("
            + DefinitionsEntry.COLUMN_LINK_FOLDER_ID + " INT NOT NULL, "
            + DefinitionsEntry.COLUMN_LINK_WORD_CARD_ID + " INT NOT NULL); ";

    //DB_VERSION = 2
    private static final String SQL_ALTER_TABLE_WORD_CARD_CORRECT_TOTAL =
            "ALTER TABLE " +
            DefinitionsEntry.TABLE_NAME_WORD_CARDS +
            " ADD COLUMN " + DefinitionsEntry.COLUMN_WORD_CARD_CORRECT_TOTAL + " INTEGER DEFAULT 0;";

    private static final String SQL_ALTER_TABLE_WORD_CARD_CORRECT_LAST =
            "ALTER TABLE " +
            DefinitionsEntry.TABLE_NAME_WORD_CARDS +
            " ADD COLUMN " + DefinitionsEntry.COLUMN_WORD_CARD_CORRECT_LAST + " INTEGER DEFAULT 0;";

    private static final String SQL_ALTER_TABLE_WORD_CARD_WRONG_TOTAL =
            "ALTER TABLE " +
            DefinitionsEntry.TABLE_NAME_WORD_CARDS +
            " ADD COLUMN " + DefinitionsEntry.COLUMN_WORD_CARD_WRONG_TOTAL + " INTEGER DEFAULT 0;";

    private static final String SQL_ALTER_TABLE_WORD_CARD_WRONG_LAST =
            "ALTER TABLE " +
            DefinitionsEntry.TABLE_NAME_WORD_CARDS +
            " ADD COLUMN " + DefinitionsEntry.COLUMN_WORD_CARD_WRONG_LAST + " INTEGER DEFAULT 0;";


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
        //DB_VERSION = 2 (statistics fields added)
        if (oldVersion < 2)
        {
            db.execSQL(SQL_ALTER_TABLE_WORD_CARD_CORRECT_TOTAL);
            db.execSQL(SQL_ALTER_TABLE_WORD_CARD_CORRECT_LAST);
            db.execSQL(SQL_ALTER_TABLE_WORD_CARD_WRONG_TOTAL);
            db.execSQL(SQL_ALTER_TABLE_WORD_CARD_WRONG_LAST);
        }
    }
}
