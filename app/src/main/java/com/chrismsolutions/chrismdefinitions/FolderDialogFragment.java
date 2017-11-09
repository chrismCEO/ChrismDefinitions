package com.chrismsolutions.chrismdefinitions;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;

import com.chrismsolutions.chrismdefinitions.data.DefinitionProvider;
import com.chrismsolutions.chrismdefinitions.data.DefinitionsContract;

/**
 * Created by Christian Myrvold on 05.11.2017.
 */

public class FolderDialogFragment extends DialogFragment
{

    public static final java.lang.String DIALOG_TAG = "dialog";
    public static final java.lang.String DIALOG_EDIT = "dialog_edit";
    public static final java.lang.String DIALOG_FOLDER_NAME = "dialog_folder_name";
    public static final java.lang.String DIALOG_FOLDER_ID = "dialog_folder_id";

    public FolderDialogFragment()
    {
        super();
    }

    /**
     * Show the dialog for either new or existing folders.
     * @param savedInstanceState
     * @return
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle arguments = getArguments();

        final boolean edit;

        final Context context = getActivity();
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        final int id;
        final String name;
        final EditText folderNameView;
        final View dialogView;

        LayoutInflater inflater = getActivity().getLayoutInflater();

        dialogView = inflater.inflate(R.layout.dialog_folder, null);
        dialogBuilder.setView(dialogView);
        folderNameView = (EditText) dialogView.findViewById(R.id.dialog_folder_name_edit);

        if (getArguments() != null)
        {
            arguments = getArguments();
            edit = arguments.getBoolean(DIALOG_EDIT);
        }
        else
        {
            edit = false;
        }

        if (edit)
        {
            //Show the current folder name
            name = arguments.getString(DIALOG_FOLDER_NAME);

            if (!TextUtils.isEmpty(name))
            {
                folderNameView.setText(name);
                folderNameView.setSelection(name.length());
            }

            //Get the current folder ID
            id = arguments.getInt(DIALOG_FOLDER_ID);

            dialogBuilder.setTitle(context.getString(R.string.edit_folder_header));
        }
        else
        {
            dialogView.findViewById(R.id.dialog_folder_name).setVisibility(View.GONE);
            id = 0;
            dialogBuilder.setTitle(context.getString(R.string.create_folder_header));
        }

        dialogBuilder.setPositiveButton(dialogBuilder.getContext().getString(R.string.ok_dialog),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String folderNameLocal = folderNameView.getText().toString().trim();
                        ContentValues values = new ContentValues();
                        values.put(DefinitionsContract.DefinitionsEntry.COLUMN_FOLDER_NAME, folderNameLocal);

                        if (!edit) {
                            context.getContentResolver().insert(DefinitionsContract.DefinitionsEntry.CONTENT_URI_FOLDER, values);
                        } else {
                            String selection = DefinitionsContract.DefinitionsEntry._ID + DefinitionProvider.getSqlJokerId();
                            String[] selectionArgs = {String.valueOf(id)};
                            context.getContentResolver().update(
                                    DefinitionsContract.DefinitionsEntry.CONTENT_URI_FOLDER,
                                    values,
                                    selection,
                                    selectionArgs);
                        }
                    }
                });

        dialogBuilder.setNegativeButton(dialogBuilder.getContext().getString(R.string.cancel_dialog),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        return dialogBuilder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }
}
