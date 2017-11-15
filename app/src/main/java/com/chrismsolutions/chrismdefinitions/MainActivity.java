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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.chrismsolutions.chrismdefinitions.data.DefinitionsContract.DefinitionsEntry;
import com.google.android.gms.ads.AdView;

public class MainActivity extends AppCompatActivity
    implements LoaderManager.LoaderCallbacks<Cursor>
{

    private static final String URI_STRING = "uri";
    private static final int LOADER_ID = 1;
    FolderCursorAdapter adapter;
    private boolean showAds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Show ads if user has not payed to suppress them
        showAds = ChrismAdHelper.showAd(this);
        if (showAds)
        {
            setContentView(R.layout.activity_main_ads);
        }
        else
        {
            setContentView(R.layout.activity_main);
        }

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

        ListView listView = (ListView) findViewById(R.id.list);
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
                startActivity(wordCardsList);
            }
        });

        getLoaderManager().initLoader(LOADER_ID, null, this);

        //For test purposes only
        //TestDB.testDB(MainActivity.this);

        //Ad management
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.relative_layout_main);
        if (showAds)
        {
            ChrismAdHelper.createAd(this, relativeLayout);
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

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.searchFolder).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.requestFocus();

        //TODO: show suggestions
        //searchView.setSuggestionsAdapter();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
}
