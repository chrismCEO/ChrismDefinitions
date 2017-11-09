package com.chrismsolutions.chrismdefinitions;

import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.chrismsolutions.chrismdefinitions.data.DefinitionProvider;
import com.chrismsolutions.chrismdefinitions.data.DefinitionsContract;

/**
 * Created by Christian Myrvold on 05.11.2017.
 */

public class WordCardDialogFragment extends DialogFragment
{
    public static final String DIALOG_WORD_CARD_EDIT = "dialog_word_card_edit";
    public static final String DIALOG_WORD_CARD_NAME = "dialog_word_card_name";
    public static final String DIALOG_WORD_CARD_DEF = "dialog_word_card_def";
    public static final String DIALOG_WORD_CARD_ID = "dialog_word_card_id";
    public static final String DIALOG_FOLDER_ID = "dialog_word_card_folder_id";
    public static final String DIALOG_TAG = "dialog";
    private static final String DIALOG_CONTINUE = "dialog_continue";


    /**
     * Show the dialog for new or existing word cards. If the checkbox is checked, continue showing
     * the dialog to quickly create new word cards
     * @param savedInstanceState
     * @return
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final Context dialogContext = getActivity();
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(dialogContext);
        final boolean edit;

        final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_word_card, null);
        final EditText wordCardName = (EditText) dialogView.findViewById(R.id.dialog_word_card_name_edit);
        final EditText wordCardDef = (EditText) dialogView.findViewById(R.id.dialog_word_card_def_edit);
        final String name;
        final String def;
        final int id;
        final int idFolder;

        Bundle arguments = getArguments();
        edit = arguments.getBoolean(DIALOG_WORD_CARD_EDIT);

        if (edit)
        {
            name = arguments.getString(DIALOG_WORD_CARD_NAME);
            wordCardName.setText(name);

            def = arguments.getString(DIALOG_WORD_CARD_DEF);
            wordCardDef.setText(def);

            id = arguments.getInt(DIALOG_WORD_CARD_ID);

            dialogBuilder.setTitle(dialogContext.getString(R.string.dialog_edit_word_card));

            wordCardName.setSelection(name.length());

            dialogView.findViewById(R.id.continue_creating_word_cards).setVisibility(View.GONE);
        }
        else
        {
            id = 0;

            CheckBox continueCheck = (CheckBox) dialogView.findViewById(R.id.continue_creating_word_cards);
            if (arguments.getBoolean(DIALOG_CONTINUE))
            {
                continueCheck.setChecked(true);
            }
            dialogBuilder.setTitle(dialogContext.getString(R.string.dialog_create_word_card));

            dialogView.findViewById(R.id.dialog_word_card_name).setVisibility(View.GONE);
            dialogView.findViewById(R.id.dialog_word_card_def).setVisibility(View.GONE);
        }

        idFolder = arguments.getInt(DIALOG_FOLDER_ID);

        dialogBuilder.setView(dialogView);

        dialogBuilder.setPositiveButton(dialogBuilder.getContext().getString(R.string.ok_dialog), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ContentValues values = new ContentValues();
                values.put(DefinitionsContract.DefinitionsEntry.COLUMN_WORD_CARD_NAME, wordCardName.getText().toString().trim());
                values.put(DefinitionsContract.DefinitionsEntry.COLUMN_WORD_CARD_TEXT, wordCardDef.getText().toString().trim());

                if (!edit)
                {
                    dialogContext.getContentResolver().insert(
                            ContentUris.withAppendedId(DefinitionsContract.DefinitionsEntry.CONTENT_URI_FOLDER,
                                    idFolder),
                            values);
                }
                else
                {
                    String selection = DefinitionsContract.DefinitionsEntry._ID + DefinitionProvider.getSqlJokerId();
                    String[] selectionArgs = {String.valueOf(id)};

                    dialogContext.getContentResolver().update(
                            DefinitionsContract.DefinitionsEntry.CONTENT_URI_WORD_CARD,
                            values,
                            selection,
                            selectionArgs);
                }

                //If checkbox is checked, we continue creating word cards
                CheckBox continueCheck = (CheckBox) dialogView.findViewById(R.id.continue_creating_word_cards);

                if (continueCheck.isChecked())
                {
                    WordCardDialogFragment fragment = new WordCardDialogFragment();
                    Bundle arguments = getArguments();
                    arguments.putBoolean(DIALOG_CONTINUE, true);
                    fragment.setArguments(arguments);
                    fragment.show(getFragmentManager(), DIALOG_TAG);
                }
            }
        });

        dialogBuilder.setNegativeButton(dialogBuilder.getContext().getString(R.string.cancel_dialog), new DialogInterface.OnClickListener() {
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
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

}
