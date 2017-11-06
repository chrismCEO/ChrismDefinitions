package com.chrismsolutions.chrismdefinitions;

import android.content.Context;
import android.database.Cursor;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.chrismsolutions.chrismdefinitions.data.DefinitionProvider;
import com.chrismsolutions.chrismdefinitions.data.DefinitionsContract.DefinitionsEntry;

/**
 * Created by Christian Myrvold on 27.10.2017.
 */

public class FolderCursorAdapter extends CursorAdapter
{
    private static final String SQL_JOKER_ID = "=?";

    public FolderCursorAdapter(Context context, Cursor cursor)
    {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        return LayoutInflater.from(context).inflate(R.layout.list_item_folders, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor)
    {
        TextView textViewFolderName = (TextView) view.findViewById(R.id.folder_name);

        final int id = cursor.getInt(cursor.getColumnIndexOrThrow(DefinitionsEntry._ID));
        int columnIndex = cursor.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_FOLDER_NAME);
        final String name = cursor.getString(columnIndex);

        textViewFolderName.setText(name);

        //Set the onClickListener on the Delete Folder image
        ImageView deleteFolder = (ImageView) view.findViewById(R.id.delete_folder);
        deleteFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelper.deleteFolderConfirmationDialog(context, id, name);
            }
        });

        //Set the onClickListener on the Edit Folder image
        ImageView editFolder = (ImageView) view.findViewById(R.id.edit_folder);
        editFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle arguments = new Bundle();
                arguments.putInt(FolderDialogFragment.DIALOG_FOLDER_ID, id);
                arguments.putString(FolderDialogFragment.DIALOG_FOLDER_NAME, name);
                arguments.putBoolean(FolderDialogFragment.DIALOG_EDIT, true);

                FragmentActivity fragmentActivity = (FragmentActivity)context;
                FolderDialogFragment fragment = new FolderDialogFragment();
                fragment.setArguments(arguments);
                fragment.show(fragmentActivity.getSupportFragmentManager(), FolderDialogFragment.DIALOG_TAG);
                //DialogHelper.newFolderDialog(context, true, id, name);
            }
        });
    }
}
