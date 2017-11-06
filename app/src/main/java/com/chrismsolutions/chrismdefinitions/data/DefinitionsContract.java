package com.chrismsolutions.chrismdefinitions.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Christian Myrvold on 27.10.2017.
 */

public final class DefinitionsContract
{
    //Content values
    public static final String CONTENT_AUTHORITY = "com.chrismsolutions.chrismdefinitions";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_DEFINITIONS_FOLDER = "folders";
    public static final String PATH_DEFINITIONS_WORD_CARDS = "wordCards";

    private DefinitionsContract()
    {
        throw new AssertionError("DefinitionsContract cannot be instantiated");
    }

    public static class DefinitionsEntry implements BaseColumns
    {
        public static final Uri CONTENT_URI_FOLDER = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_DEFINITIONS_FOLDER);
        public static final String CONTENT_LIST_TYPE_FOLDER =
                ContentResolver. CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DEFINITIONS_FOLDER;

        public static final String CONTENT_ITEM_TYPE_FOLDER =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DEFINITIONS_FOLDER;


        public static final Uri CONTENT_URI_WORD_CARD = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_DEFINITIONS_WORD_CARDS);
        public static final String CONTENT_LIST_TYPE_WORD_CARD =
                ContentResolver. CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DEFINITIONS_WORD_CARDS;

        public static final String CONTENT_ITEM_TYPE_WORD_CARD =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DEFINITIONS_WORD_CARDS;


        //Folder table and column names
        public static final String TABLE_NAME_FOLDERS = "folders";
        public static final String COLUMN_FOLDER_NAME = "name";

        //Word card table and column names
        public static final String TABLE_NAME_WORD_CARDS = "wordCards";
        public static final String COLUMN_WORD_CARD_NAME = "name";
        public static final String COLUMN_WORD_CARD_TEXT = "text";

        //Link table and column names
        public static final String TABLE_NAME_LINK = "links";
        public static final String COLUMN_LINK_WORD_CARD_ID = "wordCardId";
        public static final String COLUMN_LINK_FOLDER_ID = "folderId";
    }
}
