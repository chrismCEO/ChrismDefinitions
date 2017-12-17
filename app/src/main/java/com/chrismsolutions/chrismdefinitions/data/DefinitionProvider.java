package com.chrismsolutions.chrismdefinitions.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.chrismsolutions.chrismdefinitions.R;
import com.chrismsolutions.chrismdefinitions.data.DefinitionsContract.DefinitionsEntry;

import java.lang.reflect.Array;
import java.util.HashMap;

/**
 * Created by Christian Myrvold on 27.10.2017.
 */

public class DefinitionProvider extends ContentProvider
{
    private static final String SQL_JOKER_ID = "=?";
    private static final String LOG_TAG = DefinitionProvider.class.getName();

    private DefinitionDBHelper mDBHelper;

    private static final int FOLDER_ITEMS = 100;
    private static final int FOLDER_ITEM_ID = 101;

    private static final int WORD_CARD_ITEMS = 200;
    private static final int WORD_CARD_ITEM_ID = 201;

    private static final int SUGGEST_URI_PATH_QUERY =300;
    private static final int SUGGEST_URI_PATH_QUERY_ID = 301;

    private static final String FOLDERS = "folders";
    private static final String WORD_CARDS = "wordCards";
    private static final String SUGGESTIONS = "suggestions";

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static
    {
        sUriMatcher.addURI(DefinitionsContract.CONTENT_AUTHORITY, DefinitionsContract.PATH_DEFINITIONS_FOLDER, FOLDER_ITEMS);
        sUriMatcher.addURI(DefinitionsContract.CONTENT_AUTHORITY, DefinitionsContract.PATH_DEFINITIONS_FOLDER + "/#", FOLDER_ITEM_ID);

        sUriMatcher.addURI(DefinitionsContract.CONTENT_AUTHORITY, DefinitionsContract.PATH_DEFINITIONS_WORD_CARDS, WORD_CARD_ITEMS);
        sUriMatcher.addURI(DefinitionsContract.CONTENT_AUTHORITY, DefinitionsContract.PATH_DEFINITIONS_WORD_CARDS + "/#", WORD_CARD_ITEM_ID);

        sUriMatcher.addURI(DefinitionsContract.CONTENT_AUTHORITY, DefinitionsContract.PATH_DEFINITIONS_SUGGESTIONS, SUGGEST_URI_PATH_QUERY);
        sUriMatcher.addURI(DefinitionsContract.CONTENT_AUTHORITY, DefinitionsContract.PATH_DEFINITIONS_SUGGESTIONS + "/#", SUGGEST_URI_PATH_QUERY_ID);
    }


    private static final HashMap<String, String> sFolderProjectionMap;
    private static final HashMap<String, String> sWordCardProjectionMap;
    private static final HashMap<String, String> sLinkProjectionMap;

    static
    {
        sFolderProjectionMap = new HashMap<String, String>();
        sFolderProjectionMap.put(DefinitionsEntry._ID, DefinitionsEntry.TABLE_NAME_FOLDERS + "." + DefinitionsEntry._ID);
        sFolderProjectionMap.put(DefinitionsEntry.COLUMN_FOLDER_NAME, DefinitionsEntry.TABLE_NAME_FOLDERS + "." + DefinitionsEntry.COLUMN_FOLDER_NAME);

        sWordCardProjectionMap = new HashMap<String, String>();
        sWordCardProjectionMap.put(DefinitionsEntry._ID, DefinitionsEntry.TABLE_NAME_WORD_CARDS + "." + DefinitionsEntry._ID);
        sWordCardProjectionMap.put(DefinitionsEntry.COLUMN_WORD_CARD_NAME, DefinitionsEntry.TABLE_NAME_WORD_CARDS + "." + DefinitionsEntry.COLUMN_WORD_CARD_NAME);
        sWordCardProjectionMap.put(DefinitionsEntry.COLUMN_WORD_CARD_TEXT, DefinitionsEntry.TABLE_NAME_WORD_CARDS + "." + DefinitionsEntry.COLUMN_WORD_CARD_TEXT);

        sLinkProjectionMap = new HashMap<String, String>();
        sLinkProjectionMap.put(DefinitionsEntry.COLUMN_LINK_FOLDER_ID, DefinitionsEntry.TABLE_NAME_LINK + "." + DefinitionsEntry.COLUMN_LINK_FOLDER_ID);
        sLinkProjectionMap.put(DefinitionsEntry.COLUMN_LINK_WORD_CARD_ID, DefinitionsEntry.TABLE_NAME_LINK + "." + DefinitionsEntry.COLUMN_LINK_WORD_CARD_ID);
    }

    private Uri uriGlobal;
    private WordSuggestionDB mDictionary;

    @Override
    public boolean onCreate()
    {
        mDBHelper = new DefinitionDBHelper(getContext());
        mDictionary = new WordSuggestionDB(getContext());
        return true;
    }

    /**
     * Perform a query on the database.
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return Cursor of the result of the query
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri,
                        @Nullable String[] projection,
                        @Nullable String selection,
                        @Nullable String[] selectionArgs,
                        @Nullable String sortOrder)
    {
        SQLiteQueryBuilder database = new SQLiteQueryBuilder();
        Cursor cursor = null, suggestionCursor = null;
        int match = sUriMatcher.match(uri);
        String type = "";
        boolean skipQuery = false;

        switch (match)
        {
            case FOLDER_ITEMS:
                database.setTables(DefinitionsEntry.TABLE_NAME_FOLDERS);
                database.setProjectionMap(sFolderProjectionMap);
                type = FOLDERS;
                break;

            case FOLDER_ITEM_ID:
                selection = DefinitionsEntry._ID + SQL_JOKER_ID;
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                database.setTables(DefinitionsEntry.TABLE_NAME_FOLDERS);
                database.setProjectionMap(sFolderProjectionMap);
                database.appendWhere(DefinitionsEntry.TABLE_NAME_FOLDERS + "." + DefinitionsEntry._ID + "=" +
                        String.valueOf(ContentUris.parseId(uri)));
                type = FOLDERS;
                break;

            case WORD_CARD_ITEMS:
                if (selectionArgs != null)
                {
                    if (selection == null)
                    {
                        selection = DefinitionsEntry.TABLE_NAME_LINK + "."
                                + DefinitionsEntry.COLUMN_LINK_FOLDER_ID + SQL_JOKER_ID;
                    }

                    if (selectionArgs.length > 1)
                    {
                        StringBuilder builder = new StringBuilder();
                        builder.append(DefinitionsEntry.TABLE_NAME_LINK + "." +
                                DefinitionsEntry.COLUMN_LINK_FOLDER_ID + " IN(?");

                        for (int i = 1; i < selectionArgs.length; i++)
                        {
                            builder.append(",?");
                        }

                        builder.append(")");
                        selection = builder.toString();
                    }

                }

                //We need a join with the Link table based on the folder ID
                database.setTables(DefinitionsEntry.TABLE_NAME_WORD_CARDS +
                        " LEFT OUTER JOIN " + DefinitionsEntry.TABLE_NAME_LINK + " ON " +
                        DefinitionsEntry.TABLE_NAME_WORD_CARDS + "." + DefinitionsEntry._ID + "=" +
                        DefinitionsEntry.TABLE_NAME_LINK + "." + DefinitionsEntry.COLUMN_LINK_WORD_CARD_ID);
                //database.setProjectionMap(sWordCardProjectionMap);
                type = WORD_CARDS;
                break;

            case WORD_CARD_ITEM_ID:
                //We need a join with the Link table based on the folder ID
                if (ContentUris.parseId(uri) != 0)
                {
                    database.setTables(
                            DefinitionsEntry.TABLE_NAME_WORD_CARDS +
                                    " LEFT OUTER JOIN " + DefinitionsEntry.TABLE_NAME_LINK + " ON " +
                                    DefinitionsEntry.TABLE_NAME_WORD_CARDS + "." + DefinitionsEntry._ID + "=" +
                                    DefinitionsEntry.TABLE_NAME_LINK + "." + DefinitionsEntry.COLUMN_LINK_WORD_CARD_ID);
                    if (selection != null)
                    {
                        selection += " AND " + DefinitionsEntry.TABLE_NAME_LINK + "."
                                + DefinitionsEntry.COLUMN_LINK_FOLDER_ID + SQL_JOKER_ID;
                    }
                    else
                    {
                        selection = DefinitionsEntry.TABLE_NAME_LINK + "."
                                + DefinitionsEntry.COLUMN_LINK_FOLDER_ID + SQL_JOKER_ID;
                    }
                    if (selectionArgs != null)
                    {
                        selectionArgs = new String[]{selectionArgs[0], String.valueOf(ContentUris.parseId(uri))};
                    }
                    else
                    {
                        selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                    }
                }
                else
                {
                    database.setTables(DefinitionsEntry.TABLE_NAME_WORD_CARDS);
                    database.setProjectionMap(sWordCardProjectionMap);
                }
                type = WORD_CARDS;
                break;

            case SUGGEST_URI_PATH_QUERY:
            case SUGGEST_URI_PATH_QUERY_ID:
                suggestionCursor = getSuggestions(selectionArgs[0]);
                //database.setProjectionMap(sWordCardProjectionMap);
                type = SUGGESTIONS;
                break;

            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        //set the order by
        if (TextUtils.isEmpty(sortOrder) && type != SUGGESTIONS)
        {
            switch (type)
            {
                case FOLDERS:
                    sortOrder = DefinitionsEntry.TABLE_NAME_FOLDERS + "." + DefinitionsEntry.COLUMN_FOLDER_NAME + " ASC";
                    break;

                case  WORD_CARDS:
                    sortOrder = DefinitionsEntry.TABLE_NAME_WORD_CARDS + "." + DefinitionsEntry.COLUMN_WORD_CARD_NAME + " ASC";
                    break;

                default:
                    throw new UnknownError("Unknown table type for sort order " + type);
            }
        }

        if (type.equals(SUGGESTIONS))
        {
            cursor = suggestionCursor;
        }
        else
        {
            cursor = database.query(
                    mDBHelper.getReadableDatabase(),
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder);
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return cursor;
    }

    private Cursor getSuggestions(String query)
    {
        query = query.toLowerCase();
        String[] projections = new String[]
            {
                    BaseColumns._ID,
                    WordSuggestionDB.KEY_WORD,
                    WordSuggestionDB.KEY_DEFINITION
            };

        return mDictionary.getWordMatches(query, projections);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        String type = "";

        switch (match)
        {
            case FOLDER_ITEMS:
                type = DefinitionsEntry.CONTENT_LIST_TYPE_FOLDER;
                break;

            case FOLDER_ITEM_ID:
                type = DefinitionsEntry.CONTENT_ITEM_TYPE_FOLDER;

            case WORD_CARD_ITEMS:
            case WORD_CARD_ITEM_ID:
                type = DefinitionsEntry.CONTENT_ITEM_TYPE_WORD_CARD;
                break;

            case SUGGEST_URI_PATH_QUERY:
            case SUGGEST_URI_PATH_QUERY_ID:
                type = DefinitionsEntry.CONTENT_LIST_TYPE_SUGGESTION;
                break;

            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
        return type;
    }

    /**
     * Insert new data into either the folders or wordCards table. When inserting into the wordCards
     * table we also need to insert a link between the folder and the word card
     * @param uri
     * @param values
     * @return A Uri with the ID of the newly created post
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values)
    {
        Uri uriLocal = null;

        final int match = sUriMatcher.match(uri);

        switch (match)
        {
            case FOLDER_ITEMS:
                uriLocal = insertTable(DefinitionsEntry.TABLE_NAME_FOLDERS, uri, values);
                break;
            case FOLDER_ITEM_ID:
                uriLocal = insertTable(DefinitionsEntry.TABLE_NAME_WORD_CARDS, DefinitionsEntry.CONTENT_URI_WORD_CARD, values);
                insertLink(uriLocal, ContentUris.parseId(uri));
                break;

            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }

        return uriLocal;
    }

    /**
     * Insert a link between the folder and the word card.
     * @param uriLocal
     * @param folderId
     */
    private void insertLink(Uri uriLocal, long folderId)
    {
        SQLiteDatabase database = mDBHelper.getWritableDatabase();

        ContentValues valuesLocal = new ContentValues();
        if (uriLocal != null)
        {
            valuesLocal.put(DefinitionsEntry.COLUMN_LINK_WORD_CARD_ID, String.valueOf(ContentUris.parseId(uriLocal)));
            valuesLocal.put(DefinitionsEntry.COLUMN_LINK_FOLDER_ID, folderId);

            database.insert(DefinitionsEntry.TABLE_NAME_LINK, null, valuesLocal);
        }
    }

    /**
     * Insert a new record into the given table with the user-given values
     * @param tableName
     * @param uri
     * @param values
     * @return
     */
    private Uri insertTable(String tableName, Uri uri, ContentValues values)
    {
        Uri uriLocal = null;
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        long id = 0;

        if (checkValues(tableName, values))
        {
            id = database.insert(tableName, null, values);

            if (id == -1)
            {
                Log.e(LOG_TAG, "Failed to insert row for " + uri);
            }
            else
            {
                uriLocal = ContentUris.withAppendedId(uri, id);
                getContext().getContentResolver().notifyChange(uriLocal, null);
            }
        }

        return uriLocal;
    }

    /**
     * Deletion of a specific record. If the record is of wordCard, we also need to delete the
     * link record. If the record is a folder, delete all links to that folder, and then check to
     * see if the wordCards are not associated to another folder.
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return A count of the number of records deleted
     */
    @Override
    public int delete(@NonNull Uri uri,
                      @Nullable String selection,
                      @Nullable String[] selectionArgs)
    {
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        int result = 0;

        final int match = sUriMatcher.match(uri);

        switch (match)
        {
            case FOLDER_ITEMS:
                result = database.delete(DefinitionsEntry.TABLE_NAME_FOLDERS, selection, selectionArgs);

                if (result != 0)
                {
                    //also delete the link entry
                    deleteLink(database, selectionArgs, DefinitionsEntry.COLUMN_LINK_FOLDER_ID);
                }
                break;

            case WORD_CARD_ITEMS:
                String[] selectionArgsLink = selectionArgs;
                if (Array.getLength(selectionArgs) > 1)
                {
                    selectionArgs = new String[] {String.valueOf(selectionArgs[0])};
                }
                result = database.delete(DefinitionsEntry.TABLE_NAME_WORD_CARDS, selection, selectionArgs);

                if (result != 0)
                {
                    //also delete the link entry
                    deleteLink(database, selectionArgsLink, DefinitionsEntry.COLUMN_LINK_WORD_CARD_ID);
                }
                break;

            case FOLDER_ITEM_ID:
                selection = DefinitionsEntry._ID + SQL_JOKER_ID;
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                result = database.delete(DefinitionsEntry.TABLE_NAME_FOLDERS, selection, selectionArgs);

                if (result != 0)
                {
                    //also delete the link entry
                    deleteLink(database, selectionArgs, DefinitionsEntry.COLUMN_LINK_FOLDER_ID);
                }
                break;

            case WORD_CARD_ITEM_ID:
                selection = DefinitionsEntry._ID + SQL_JOKER_ID;
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                result = database.delete(DefinitionsEntry.TABLE_NAME_WORD_CARDS, selection, selectionArgs);
                if (result != 0)
                {
                    //also delete the link entry
                    selection = DefinitionsEntry.COLUMN_LINK_WORD_CARD_ID + SQL_JOKER_ID;
                    deleteLink(database, selectionArgs, DefinitionsEntry.COLUMN_LINK_WORD_CARD_ID);
                }
                break;

            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (result != 0)
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return result;
    }

    /**
     * Setup the deletion of link records
     * @param database
     * @param selectionArgs
     * @param linkType
     */
    private void deleteLink(SQLiteDatabase database, String[] selectionArgs, String linkType)
    {
        String selection = linkType + SQL_JOKER_ID;

        if (Array.getLength(selectionArgs) > 1)
        {
            selection = linkType + SQL_JOKER_ID; //selectionArgs[0];
            selection += " AND " + DefinitionsEntry.COLUMN_LINK_FOLDER_ID + SQL_JOKER_ID; //" = " + selectionArgs[1];
        }

        deleteLinkWordCards(database, linkType, selection, selectionArgs);
    }

    /**
     * Go through all the link records and check if they are unique. If they are, delete the wordCards
     * associated link, otherwise only delete the link.
     * @param database
     * @param linkType
     * @param selection
     * @param selectionArgs
     */
    private void deleteLinkWordCards(SQLiteDatabase database,
                                     String linkType,
                                     String selection,
                                     String[] selectionArgs)
    {
        SQLiteDatabase dbQueryLink = mDBHelper.getReadableDatabase();
        String[] projection = {
                DefinitionsEntry.COLUMN_LINK_FOLDER_ID,
                DefinitionsEntry.COLUMN_LINK_WORD_CARD_ID
        };

        Cursor cursor = dbQueryLink.query(
                DefinitionsEntry.TABLE_NAME_LINK,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null);

        while (cursor.moveToNext())
        {
            //Count how many times this word is used
            SQLiteDatabase dbCount = mDBHelper.getReadableDatabase();
            String selectionCount = DefinitionsEntry.COLUMN_LINK_WORD_CARD_ID + DefinitionProvider.getSqlJokerId();
            String[] selectionArgsCount = {String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_LINK_WORD_CARD_ID)))};
            String[] projectionCount = {DefinitionsEntry.COLUMN_LINK_WORD_CARD_ID};

            Cursor cursorCount = dbCount.query(
                    DefinitionsEntry.TABLE_NAME_LINK,
                    projectionCount,
                    selectionCount,
                    selectionArgsCount,
                    null,
                    null,
                    null);

            cursorCount.moveToNext();

            if (cursorCount.getCount() == 1)
            {
                //We only use this word card in this context, delete both link and word card
                //Delete link table entry
                int result = database.delete(DefinitionsEntry.TABLE_NAME_LINK, selectionCount, selectionArgsCount);

                //Delete word card associated with link table entry
                String selectionWordCard = DefinitionsEntry._ID + DefinitionProvider.getSqlJokerId();
                String[] selectionArgsWordCard = {String.valueOf(selectionArgsCount[0])};
                database.delete(DefinitionsEntry.TABLE_NAME_WORD_CARDS, selectionWordCard, selectionArgsWordCard);
            }
            else
            {
                //We use this word card in more contexts, delete only the link
                //This will not happen yet, but when word cards can be copied in a later version,
                //this, should just work
                String selectionLink = DefinitionsEntry.COLUMN_LINK_FOLDER_ID + DefinitionProvider.getSqlJokerId() +
                        " AND " + DefinitionsEntry.COLUMN_LINK_WORD_CARD_ID + DefinitionProvider.getSqlJokerId();
                String[] selectionArgsLink = {
                        String.valueOf(cursor.getInt(
                                cursor.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_LINK_FOLDER_ID))),
                        String.valueOf(cursor.getInt(
                                cursor.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_LINK_WORD_CARD_ID))),
                };
            }
        }
        cursor.close();
    }

    /**
     * Update the given record with the user-given values. This is straight-forward
     * @param uri
     * @param values
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public int update(@NonNull Uri uri,
                      @Nullable ContentValues values,
                      @Nullable String selection,
                      @Nullable String[] selectionArgs)
    {
        final int match = sUriMatcher.match(uri);
        int result = 0;
        String tableName = "";

        switch (match)
        {
            case FOLDER_ITEMS:
                tableName = DefinitionsEntry.TABLE_NAME_FOLDERS;
                break;

            case WORD_CARD_ITEMS:
                tableName = DefinitionsEntry.TABLE_NAME_WORD_CARDS;
                break;

            case FOLDER_ITEM_ID:
                tableName = DefinitionsEntry.TABLE_NAME_FOLDERS;
                selection = DefinitionsEntry._ID + SQL_JOKER_ID;
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                break;

            case WORD_CARD_ITEM_ID:
                tableName = DefinitionsEntry.TABLE_NAME_WORD_CARDS;
                selection = DefinitionsEntry._ID + SQL_JOKER_ID;
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                break;

            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
        result = updateTable(tableName, uri, values, selection, selectionArgs);

        if (result != 0)
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return result;
    }

    /**
     * Update the given record(s) if the values are valid
     * @param tableName
     * @param uri
     * @param values
     * @param selection
     * @param selectionArgs
     * @return
     */
    private int updateTable(String tableName,
                            Uri uri,
                            ContentValues values,
                            String selection,
                            String[] selectionArgs)
    {
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        int id = 0;

        if (values.size() > 0 && checkValues(tableName, values))
        {
            id = database.update(tableName, values, selection, selectionArgs);

            if (id == -1)
            {
                Log.e(LOG_TAG, "Failed to update row for " + uri);
                id = 0;
            }
            else
            {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }

        return id;
    }

    /**
     * Check the values given by the user to see if they are valid
     * @param tableName
     * @param values
     * @return
     */
    private boolean checkValues(String tableName, ContentValues values)
    {
        boolean ok = true;

        switch (tableName)
        {
            case DefinitionsEntry.TABLE_NAME_FOLDERS:
                ok = checkFolderValues(values);
                break;

            case DefinitionsEntry.TABLE_NAME_WORD_CARDS:
                ok = checkWordCardValues(values);
                break;

            default:
                throw new IllegalArgumentException("Wrong table name given: " + tableName);
        }

        return ok;
    }

    /**
     * Check the values for word cards. Name and text cannot be empty
     * @param values
     * @return
     */
    private boolean checkWordCardValues(ContentValues values)
    {
        boolean ok = true;
        if (values.containsKey(DefinitionsEntry.COLUMN_WORD_CARD_NAME))
        {
            String wordCardName = values.getAsString(DefinitionsEntry.COLUMN_WORD_CARD_NAME);
            if (TextUtils.isEmpty(wordCardName))
            {
                Toast.makeText(
                        getContext(),
                        getContext().getString(R.string.empty_word_card_name_check),
                        Toast.LENGTH_SHORT).show();
                ok = false;
            }
        }

        if (values.containsKey(DefinitionsEntry.COLUMN_WORD_CARD_TEXT))
        {
            String wordCardDef = values.getAsString(DefinitionsEntry.COLUMN_WORD_CARD_TEXT);
            if (TextUtils.isEmpty(wordCardDef))
            {
                Toast.makeText(
                        getContext(),
                        getContext().getString(R.string.empty_word_card_text_check),
                        Toast.LENGTH_SHORT).show();
                ok = false;
            }
        }
        return ok;
    }

    /**
     * Check the values for folders. Name cannot be empty
     * @param values
     * @return
     */
    private boolean checkFolderValues(ContentValues values)
    {
        boolean ok = true;
        String folderName = values.getAsString(DefinitionsEntry.COLUMN_FOLDER_NAME);

        if (TextUtils.isEmpty(folderName))
        {
            Toast.makeText(
                    getContext(),
                    getContext().getString(R.string.empty_folder_name_check),
                    Toast.LENGTH_SHORT).show();
            ok = false;
        }

        return ok;
    }

    static public String getSqlJokerId()
    {
        return SQL_JOKER_ID;
    }
}
