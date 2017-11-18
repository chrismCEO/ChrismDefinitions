package com.chrismsolutions.chrismdefinitions;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.chrismsolutions.chrismdefinitions.billingUtil.IabHelper;
import com.chrismsolutions.chrismdefinitions.data.DefinitionsContract.DefinitionsEntry;
import com.chrismsolutions.chrismdefinitions.data.WordSuggestionDB;

public class MainActivity extends AppCompatActivity
    implements LoaderManager.LoaderCallbacks<Cursor>
{

    private static final String URI_STRING = "uri";
    private static final int LOADER_ID = 1;
    public static final String IS_PREMIUM_USER = "IS_PREMIUM_USER";
    FolderCursorAdapter adapter;
    ListView listView;
    private boolean showAds;
    ChrismAdHelper adHelper;
    IabHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Show ads if user has not payed to suppress them
        if (getIntent() != null && getIntent().hasExtra(MainActivity.IS_PREMIUM_USER))
        {
            showAds = !getIntent().getBooleanExtra(MainActivity.IS_PREMIUM_USER, false);
            if (showAds)
            {
                adHelper = new ChrismAdHelper(this, true, true);
            }
        }
        else
        {
            adHelper = new ChrismAdHelper(this, true, false);
        }

        if (showAds)
        {
            setContentView(R.layout.activity_main_ads);
        }
        else
        {
            setContentView(R.layout.activity_main);
        }
        //adHelper = new ChrismAdHelper(this, true);
        /*showAds = adHelper.showAd();
        if (showAds)
        {
            RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.relative_layout_main);
            adHelper.createAd(relativeLayout);
        }
        else
        {
            setContentView(R.layout.activity_main);
        }

        setContentView(R.layout.activity_main);*/

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Show the logo in the action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setIcon(R.mipmap.round_logo);
        }

        //Create new folder when fab is clicked
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FolderDialogFragment fragment = new FolderDialogFragment();
                fragment.show(getSupportFragmentManager(), FolderDialogFragment.DIALOG_TAG);
            }
        });

        listView = (ListView) findViewById(R.id.list);
        listView.setEmptyView(findViewById(R.id.empty_view));

        adapter = new FolderCursorAdapter(this, readFolderDataFromDB());
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Open definition list for this folder
                Uri uri = Uri.withAppendedPath(Uri.parse(DefinitionsEntry.CONTENT_ITEM_TYPE_WORD_CARD), String.valueOf(id));
                Intent wordCardsList = new Intent(MainActivity.this, WordCardActivity.class);
                wordCardsList.putExtra(URI_STRING, uri);
                wordCardsList.putExtra(IS_PREMIUM_USER, !showAds);
                startActivity(wordCardsList);
            }
        });

        getLoaderManager().initLoader(LOADER_ID, null, this);

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.relative_layout_main);
        if (showAds)
        {
            adHelper.createAd(relativeLayout);//  = ChrismAdHelper.createAdStatic(this, relativeLayout);
        }

        //For test purposes only
        //TestDB.testDB(MainActivity.this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (mHelper == null)
        {
            mHelper = adHelper.getIabHelper();
        }
        if (!mHelper.handleActivityResult(requestCode, resultCode, data))
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
        else
        {
            changeDesign();
        }
    }

    public void changeDesign()
    {
        //There's been a change in ownership, change the layout
        if (adHelper.showAd())
        {
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            intent.putExtra(MainActivity.IS_PREMIUM_USER, false);
            startActivity(intent);
        }
        else if(showAds && !adHelper.showAd())
        {
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            intent.putExtra(MainActivity.IS_PREMIUM_USER, true);
            startActivity(intent);
        }
    }

    /**
     * Send a query to the database for all folders
     * @return A cursor with the data of all folders
     */
    @org.jetbrains.annotations.Contract(pure = true)
    private Cursor readFolderDataFromDB()
    {
        String[] projection = {
                DefinitionsEntry._ID,
                DefinitionsEntry.COLUMN_FOLDER_NAME
        };

        return getContentResolver().query(
                DefinitionsEntry.CONTENT_URI_FOLDER,
                projection,
                null,
                null,
                null);
    }

    /**
     * Attach a SearchView to the menu, so that it appears in the Toolbar
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        if (showAds)
        {
            MenuItem removeAdMenuItem = (MenuItem) menu.findItem(R.id.remove_ads);
            //removeAdMenuItem.setVisible(adHelper.showAd());
        }

        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.searchFolder).getActionView();

        //Include showAds
        Bundle appData = new Bundle();
        appData.putBoolean(MainActivity.IS_PREMIUM_USER, !showAds);
        //noinspection RestrictedApi
        searchView.setAppSearchData(appData);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.requestFocus();

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return true;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = searchView.getSuggestionsAdapter().getCursor();
                cursor.moveToPosition(position);
                String query = cursor.getString(1);
                searchView.setQuery(query, true);
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query)
            {
                if (query.length() > 0)
                {
                    Intent searchIntent = new Intent(MainActivity.this, WordCardActivity.class);
                    searchIntent.setAction(Intent.ACTION_SEARCH);
                    searchIntent.putExtra(SearchManager.QUERY, query);
                    startActivity(searchIntent);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                boolean show = false;
                if (newText.length() >= 2)
                {
                    searchView.setSuggestionsAdapter(getSuggestionsAdapter(newText));
                    show = true;
                }
                return show;
            }
        });

        return true;
    }

    private android.support.v4.widget.CursorAdapter getSuggestionsAdapter(String query)
    {
        String[] from = new String[]
            {
                    WordSuggestionDB.KEY_WORD,
                    WordSuggestionDB.KEY_DEFINITION
            };

        int[] to = new int[]
            {
                    R.id.word,
                    R.id.definition
            };

        String[] selectionArgs = new String[]{query};

        Cursor cursor = getContentResolver().query(
                DefinitionsEntry.CONTENT_URI_SUGGESTION,
                from,
                null,
                selectionArgs,
                null);

        android.support.v4.widget.CursorAdapter adapter = new android.support.v4.widget.SimpleCursorAdapter(
                this,
                R.layout.suggestion,
                cursor,
                from,
                to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );

        return adapter;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        boolean result = false;

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.remove_ads)
        {
            result = adHelper.removeAds();
        }

        return super.onOptionsItemSelected(item) && result;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                DefinitionsEntry._ID,
                DefinitionsEntry.COLUMN_FOLDER_NAME
        };

        return new CursorLoader(
                this,
                DefinitionsEntry.CONTENT_URI_FOLDER,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        adapter.swapCursor(null);
    }

    static public String getUriString()
    {
        return URI_STRING;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adHelper != null)
        {
            IabHelper mHelper = adHelper.getIabHelper();
            if (mHelper != null) {
                try {
                    mHelper.dispose();
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                }
            }
            mHelper = null;
        }
    }

    public void removeAds(MenuItem item)
    {
        adHelper.removeAds();
    }

    public void showSpinner(boolean show)
    {
        ProgressBar spinner = (ProgressBar) findViewById(R.id.wait);
        if (show)
        {
            spinner.setVisibility(View.VISIBLE);
        }
        else
        {
            spinner.setVisibility(View.GONE);
        }
    }
}
