package com.chrismsolutions.chrismdefinitions;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.chrismsolutions.chrismdefinitions.data.DefinitionProvider;
import com.chrismsolutions.chrismdefinitions.data.DefinitionsContract.DefinitionsEntry;

/**
 * Created by Christian Myrvold on 30.10.2017.
 */

public class DialogHelper
{
    private static final String SQL_JOKER_ID = "=?";

    /**
     * Show a dialog to have the user confirm they actually wish to delete the folder. If OK, call
     * the delete method in the provider.
     * @param context
     * @param id
     * @param folderName
     */
    public static void deleteFolderConfirmationDialog(final Context context,
                                                      final int id,
                                                      final String folderName)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setMessage(context.getString(R.string.delete_folder, folderName));

        dialogBuilder.setPositiveButton(context.getString(R.string.delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selection = DefinitionsEntry._ID + SQL_JOKER_ID;
                String[] selectionArgs = {String.valueOf(id)};
                int result = context.getContentResolver().delete(
                        DefinitionsEntry.CONTENT_URI_FOLDER, selection, selectionArgs);

                if (result > 0)
                {
                    Toast.makeText(context, context.getString(R.string.folder_deleted, folderName), Toast.LENGTH_SHORT);
                }
            }
        });

        dialogBuilder.setNegativeButton(context.getString(R.string.cancel_dialog), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null)
                {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    /**
     * Show a dialog to have the user confirm they actually wish to delete the word card. If OK, call
     * the delete method in the provider.
     * @param context
     * @param wordCardId
     * @param folderId
     * @param wordName
     */
    public static void deleteWordCardConfirmationDialog(final Context context,
                                                        final int wordCardId,
                                                        final int folderId,
                                                        final String wordName)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setMessage(context.getString(R.string.delete_word_dialog, wordName));

        dialogBuilder.setPositiveButton(context.getString(R.string.delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selection = DefinitionsEntry._ID + DefinitionProvider.getSqlJokerId();
                String[] selectionArgs = {
                        String.valueOf(wordCardId),
                        String.valueOf(folderId)
                };
                context.getContentResolver().delete(DefinitionsEntry.CONTENT_URI_WORD_CARD, selection, selectionArgs);
            }
        });

        dialogBuilder.setNegativeButton(context.getString(R.string.cancel_dialog), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }
}
