package com.chrismsolutions.chrismdefinitions;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.chrismsolutions.chrismdefinitions.data.DefinitionsContract.DefinitionsEntry;

import java.util.ArrayList;

public class CreateTestActivity extends AppCompatActivity
{
    CreateTestFolderCursorAdapter adapter;
    ArrayList<Folder> folders;
    int numberAvailable;
    TextView numberAvailableTextView;
    EditText numberChooser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_test);

        //Avoid focus on EditText
        numberChooser = (EditText) findViewById(R.id.create_test_edit_number);
        numberChooser.setSelected(false);

        numberAvailableTextView = (TextView)findViewById(R.id.create_test_total_words);
        String numAvailString = " " + numberAvailable;
        numberAvailableTextView.setText(numAvailString);

        numberChooser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                String numberString = s.toString();
                if (TextUtils.isEmpty(s))
                {
                    numberString = "0";
                }

                int numberChosen = Integer.parseInt(String.valueOf(numberString));

                if (numberChosen > numberAvailable)
                {
                    //s = s.subSequence(start, s.length()-2);
                    numberChooser.setText("");
                    Toast.makeText(
                            CreateTestActivity.this,
                            getString(R.string.number_larger_than_available),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        ListView listView = (ListView) findViewById(R.id.folder_list_create_test);

        Cursor folders = getFoldersFromDB();
        adapter = new CreateTestFolderCursorAdapter(CreateTestActivity.this, folders);

        listView.setAdapter(adapter);

        createFolderArray(folders);
    }

    private void createFolderArray(Cursor folderCursor)
    {
        folders = new ArrayList<>();
        while (folderCursor.moveToNext())
        {
            String folderName = folderCursor.getString(
                    folderCursor.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_FOLDER_NAME));
            int id = folderCursor.getInt(folderCursor.getColumnIndexOrThrow(DefinitionsEntry._ID));
            int wordCount = getWordCount(id);
            folders.add(new Folder(folderName, wordCount, id));
        }
    }

    private int getWordCount(int id)
    {
        int count = 0;
        Cursor cursor = getContentResolver().query(
                ContentUris.withAppendedId(DefinitionsEntry.CONTENT_URI_WORD_CARD, id),
                null,
                null,
                null,
                null
        );

        if (cursor != null)
        {
            count = cursor.getCount();
        }

        return count;
    }

    private Cursor getFoldersFromDB()
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

    public void folderCheckedChange(int id)
    {
        numberAvailable = 0;
        for (int i = 0; i < folders.size(); i++)
        {
            Folder folder = folders.get(i);
            if (folder.isChecked())
            {
                numberAvailable += folder.count;
            }
        }
        String numAvailString = " " + numberAvailable;
        numberAvailableTextView.setText(numAvailString);
    }

    private boolean folderChecked()
    {
        SparseBooleanArray checkStates = adapter.getCheckStates();
        boolean checked = false;

        for (int i = 0; i < folders.size(); i++)
        {
            if (adapter.getChecked(folders.get(i).id))
            {
                checked = true;
                break;
            }
        }
        return checked;
    }

    public void btnStartTest(View view)
    {
        boolean ok = true;
        if (!folderChecked())
        {
            Toast.makeText(
                    CreateTestActivity.this,
                    R.string.folder_checked_check,
                    Toast.LENGTH_SHORT)
                    .show();
            ok = false;
        }
        if (numberChooser.getText().length() == 0)
        {
            Toast.makeText(
                    CreateTestActivity.this,
                    R.string.choose_number_words_check,
                    Toast.LENGTH_SHORT)
                    .show();
            ok = false;
        }

        if (ok)
        {
            Intent intent = new Intent(CreateTestActivity.this, WordCardFlipActivity.class);
            ArrayList<Integer> ids = new ArrayList<>();

            for (int i = 0; i < folders.size(); i++)
            {
                if (adapter.getChecked(folders.get(i).id))
                {
                    ids.add(folders.get(i).id);
                }
            }

            Bundle bundle = new Bundle();
            bundle.putIntegerArrayList(WordCardFlipActivity.INTENT_ADAPTER, ids);
            intent.putExtras(bundle);
            intent.putExtra(WordCardFlipActivity.INTENT_WORD_DRAW, Integer.parseInt(numberChooser.getText().toString()));
            startActivity(intent);
        }
    }

    private class Folder
    {
        String name;
        int count, id;

        Folder(String mName, int mCount, int mId)
        {
            name = mName;
            count = mCount;
            id = mId;
        }

        boolean isChecked()
        {
            return adapter.getChecked(id);
        }
    }
}
