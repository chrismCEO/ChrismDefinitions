package com.chrismsolutions.chrismdefinitions;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.chrismsolutions.chrismdefinitions.data.DefinitionsContract.DefinitionsEntry;

import java.io.Serializable;

/**
 * Created by Christian Myrvold on 27.11.2017.
 */

public class CreateTestFolderCursorAdapter extends CursorAdapter
    implements Serializable
{
    transient private SparseBooleanArray checkStates;

    public CreateTestFolderCursorAdapter(Context context, Cursor cursor)
    {
        super(context, cursor, 0);
        checkStates = new SparseBooleanArray();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        return LayoutInflater.from(context).inflate(
                R.layout.list_item_folder_create_test,
                parent,
                false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor)
    {
        TextView folderName = view.findViewById(R.id.create_test_folder_name);
        folderName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_FOLDER_NAME)));

        final int id = cursor.getInt(cursor.getColumnIndexOrThrow(DefinitionsEntry._ID));

        TextView folderCount = view.findViewById(R.id.create_test_word_count);
        folderCount.setText(String.valueOf(getWordCount(id)));

        final CheckBox checkBox = view.findViewById(R.id.create_test_folder_check);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCheckbox(checkBox, id, context, true);
            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                onClickCheckbox(checkBox, id, context, false);
            }
        });
    }

    /**
     * User has checked a folder, store them in an array
     * @param checkBox
     * @param id
     * @param context
     * @param fromCheckbox
     */
    private void onClickCheckbox(CheckBox checkBox,
                                 int id,
                                 Context context,
                                 boolean fromCheckbox)
    {
        if (!fromCheckbox)
        {
            checkBox.setChecked(!checkBox.isChecked());
        }
        checkStates.put(id, checkBox.isChecked());

        CreateTestActivity activity = (CreateTestActivity)context;
        activity.folderCheckedChange(id);
    }

    /**
     * Count how many words are in this specific folder
     * @param id
     * @return
     */
    private int getWordCount(int id)
    {
        return mContext.getContentResolver().query(
                ContentUris.withAppendedId(DefinitionsEntry.CONTENT_URI_WORD_CARD, id),
                null,
                null,
                null,
                null
        ).getCount();
    }

    public boolean getChecked(int id)
    {
        return checkStates.get(id);
    }

    public SparseBooleanArray getCheckStates()
    {
        return checkStates;
    }
}
