package com.chrismsolutions.chrismdefinitions;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;

import com.chrismsolutions.chrismdefinitions.billingUtil.IabHelper;
import com.chrismsolutions.chrismdefinitions.data.DefinitionProvider;
import com.chrismsolutions.chrismdefinitions.data.DefinitionsContract.DefinitionsEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class WordCardActivity extends AppCompatActivity
    implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final int LOADER_ID = 2;
    private static final String SQL_JOKER_ID = "=?";
    private static final String QUERY_FOLDER_ID = "Folder";
    private Cursor folderCursor;
    private ExpandableWordCardListAdapter wordCardAdapter;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;
    private HashMap<Integer, Integer> listDataChildIds;
    private String nameQuery;

    private boolean showAds = false;
    ChrismAdHelper adHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Check appData if we come here via search
        Intent callerIntent = getIntent();
        if (callerIntent.getAction() != null && callerIntent.getAction().equals(Intent.ACTION_SEARCH))
        {
            Bundle appData = callerIntent.getBundleExtra(SearchManager.APP_DATA);
            if (appData != null)
            {
                showAds = !appData.getBoolean(MainActivity.IS_PREMIUM_USER);
            }
        }

        if (getIntent() != null && getIntent().hasExtra(MainActivity.IS_PREMIUM_USER))
        {
            showAds = !getIntent().getBooleanExtra(MainActivity.IS_PREMIUM_USER, false);
        }
        if (showAds)
        {
            setContentView(R.layout.activity_word_card_ads);
        }
        else
        {
            setContentView(R.layout.activity_word_card);
        }

        FloatingActionButton fabWordCard = findViewById(R.id.fabWordCard);
        fabWordCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle arguments = new Bundle();
                arguments.putInt(WordCardDialogFragment.DIALOG_FOLDER_ID, getFolderId());
                arguments.putBoolean(WordCardDialogFragment.DIALOG_WORD_CARD_EDIT, false);

                WordCardDialogFragment fragment = new WordCardDialogFragment();
                fragment.setArguments(arguments);
                fragment.show(getSupportFragmentManager(), WordCardDialogFragment.DIALOG_TAG);
            }
        });

        //Get the information from the caller folder
        callerIntent = getIntent();

        if (callerIntent != null)
        {
            Uri uri = callerIntent.getParcelableExtra(MainActivity.getUriString());

            if (uri != null)
            {
                int idFolder = Integer.parseInt(uri.getLastPathSegment());

                if (idFolder != 0)
                {
                    folderCursor = getFolderCursor(idFolder);

                    if (folderCursor != null && folderCursor.moveToNext())
                    {
                        //Set the title to the folder name
                        setTitle(getFolderName());
                    }
                }
            }
        }
        if (TextUtils.isEmpty(getTitle()))
        {
            setTitle(getString(R.string.title_activity_word_card));
        }

        if (callerIntent.getAction() != null && callerIntent.getAction().equals(Intent.ACTION_SEARCH))
        {
            //The user has searched for a word, get the search word
            nameQuery = callerIntent.getStringExtra(SearchManager.QUERY).trim();
            setTitle(getString(R.string.search_result_title, nameQuery.replace("%", "")));
            fabWordCard.setVisibility(View.GONE);

            //check for folder ID
            Bundle appData = callerIntent.getBundleExtra(SearchManager.APP_DATA);
            if (appData != null)
            {
                int folderId = appData.getInt(QUERY_FOLDER_ID);
                showAds = !appData.getBoolean(MainActivity.IS_PREMIUM_USER);
                if (folderId != 0)
                {
                    folderCursor = getFolderCursor(folderId);
                    if (folderCursor != null)
                    {
                        folderCursor.moveToNext();
                    }
                }
            }
        }

        //Find all word cards for this folder and display them in the list
        readWordCardDataFromDB();
        wordCardAdapter = new ExpandableWordCardListAdapter(
                this,
                listDataHeader,
                listDataChild,
                listDataChildIds,
                getFolderId());
        ExpandableListView listViewWordCards = findViewById(R.id.wordCardList);
        listViewWordCards.setAdapter(wordCardAdapter);

        listViewWordCards.setEmptyView(findViewById(R.id.empty_view_word_cards));

        //Ad management
        RelativeLayout relativeLayout = findViewById(R.id.relative_layout_word_cards);
        if (showAds)
        {
            adHelper  = ChrismAdHelper.createAdStatic(this, relativeLayout);
        }

        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    /**
     * Get the folder these words belong to.
     * @param idFolder
     * @return A cursor of the parent folder
     */
    private Cursor getFolderCursor(int idFolder)
    {
        String[] projection = {
                DefinitionsEntry._ID,
                DefinitionsEntry.COLUMN_FOLDER_NAME
        };

        String selection = DefinitionsEntry._ID + DefinitionProvider.getSqlJokerId();
        String[] selectionArgs = {String.valueOf(idFolder)};

        return getContentResolver().query(
                DefinitionsEntry.CONTENT_URI_FOLDER,
                projection,
                selection,
                selectionArgs,
                null);
    }

    /**
     * Get all word cards for the selected folder
     */
    private void readWordCardDataFromDB()
    {
        String[] projection = {
                DefinitionsEntry._ID,
                DefinitionsEntry.COLUMN_WORD_CARD_NAME,
                DefinitionsEntry.COLUMN_WORD_CARD_TEXT
        };

        Cursor cursor = null;
        String selection = null;
        String[] selectionArgs = null;
        if (!TextUtils.isEmpty(nameQuery))
        {
            //The user has searched for a word, we need to filter the wordCards table for this name
            selection = DefinitionsEntry.COLUMN_WORD_CARD_NAME + " LIKE ?";
            selectionArgs = new String[]{"%" + nameQuery + "%"};
        }
        cursor = getContentResolver().query(
                ContentUris.withAppendedId(DefinitionsEntry.CONTENT_URI_WORD_CARD, getFolderId()),
                projection,
                selection,
                selectionArgs,
                null);

        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();
        listDataChildIds = new HashMap<Integer, Integer>();
        int position = 0;

        while (cursor.moveToNext())
        {
            listDataHeader.add(getWordName(cursor));
            List<String> definition = new ArrayList<String>();
            definition.add(getWordDefinition(cursor));
            listDataChild.put(listDataHeader.get(position), definition);
            listDataChildIds.put(position, getWordId(cursor));
            position++;
        }
    }

    /**
     * Get the word card ID from the record
     * @param cursor
     * @return
     */
    private Integer getWordId(Cursor cursor)
    {
        int columnIndex = cursor.getColumnIndexOrThrow(DefinitionsEntry._ID);
        int id  = cursor.getInt(columnIndex);

        return id;
    }

    /**
     * Get the word card name from the record
     * @param cursor
     * @return
     */
    private String getWordName(Cursor cursor)
    {
        int columnIndex = cursor.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_WORD_CARD_NAME);
        String name = cursor.getString(columnIndex);

        return name;
    }

    /**
     * Get the word card text/definition from the cursor
     * @param cursor
     * @return
     */
    private String getWordDefinition(Cursor cursor)
    {
        int columnIndex = cursor.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_WORD_CARD_TEXT);
        String def = cursor.getString(columnIndex);

        return def;
    }

    /**
     * Get the folder name from the folder cursor
     * @return
     */
    private String getFolderName()
    {
        int columnIndex = folderCursor.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_FOLDER_NAME);
        String folderName = folderCursor.getString(columnIndex);
        return folderName;
    }

    /**
     * Get the folder ID from the folder cursor
     * @return
     */
    private int getFolderId()
    {
        int id = 0;
        if (folderCursor != null)
        {
            int columnIndex = folderCursor.getColumnIndexOrThrow(DefinitionsEntry._ID);
            id = folderCursor.getInt(columnIndex);
        }
        return id;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        String[] projection = {
                DefinitionsEntry.TABLE_NAME_WORD_CARDS + "." + DefinitionsEntry._ID,
                DefinitionsEntry.TABLE_NAME_WORD_CARDS + "." + DefinitionsEntry.COLUMN_WORD_CARD_NAME,
                DefinitionsEntry.TABLE_NAME_WORD_CARDS + "." + DefinitionsEntry.COLUMN_WORD_CARD_TEXT
        };

        String selection = DefinitionsEntry.TABLE_NAME_LINK + "."
                + DefinitionsEntry.COLUMN_LINK_FOLDER_ID + SQL_JOKER_ID;
        String[] selectionArgs = new String[]{String.valueOf(getFolderId())};

        return new CursorLoader(
                this,
                DefinitionsEntry.CONTENT_URI_WORD_CARD,
                projection,
                selection,
                selectionArgs,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        readWordCardDataFromDB();
        wordCardAdapter.setNewItems(listDataHeader, listDataChild, listDataChildIds);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_word_card, menu);

        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.searchFolderWordCards).getActionView();


        //Include folderId
        Bundle appData = new Bundle();
        appData.putInt(QUERY_FOLDER_ID, getFolderId());
        appData.putBoolean(MainActivity.IS_PREMIUM_USER, !showAds);
        //noinspection RestrictedApi
        searchView.setAppSearchData(appData);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.requestFocus();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() >= 2)
                {
                    searchNewQuery(newText);
                }
                else if (newText.length() == 0)
                {
                    searchNewQuery("");
                }
                return true;
            }
        });

        return true;
    }

    private void searchNewQuery(String newText)
    {
        nameQuery = newText;
        readWordCardDataFromDB();
        wordCardAdapter.setNewItems(listDataHeader, listDataChild, listDataChildIds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adHelper != null)
        {
            IabHelper mHelper = adHelper.getIabHelper();
            if (mHelper != null)
            {
                try {
                    mHelper.dispose();
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                }
            }
            mHelper = null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home)
        {
            goBack();
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    private void goBack()
    {
        Intent intent = new Intent(WordCardActivity.this, MainActivity.class);
        intent.putExtra(MainActivity.IS_PREMIUM_USER, !showAds);
        startActivity(intent);
    }
}
