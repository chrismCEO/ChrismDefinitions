package com.chrismsolutions.chrismdefinitions;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.chrismsolutions.chrismdefinitions.data.DefinitionsContract.DefinitionsEntry;

/**
 * Created by zelda on 29.10.2017.
 */

class WordCardCursorAdapter extends CursorAdapter
{

    public WordCardCursorAdapter(Context context, Cursor cursor)
    {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        return LayoutInflater.from(context).inflate(R.layout.list_item_word_cards, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor)
    {
        /*TextView textViewName = (TextView) view.findViewById(R.id.word_card_name);
        TextView textViewDefinition = (TextView) view.findViewById(R.id.word_card_text);

        int columnIndexName = cursor.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_WORD_CARD_NAME);
        int columnIndexDefinition = cursor.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_WORD_CARD_TEXT);

        final String name = cursor.getString(columnIndexName);
        final String definition = cursor.getString(columnIndexDefinition);
        final int id = cursor.getInt(cursor.getColumnIndexOrThrow(DefinitionsEntry._ID));

        textViewName.setText(name);
        textViewDefinition.setText(definition);

        ImageView editWord = (ImageView) view.findViewById(R.id.edit_word_card);
        editWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelper.newWordCardDialog(
                        context,
                        true,
                        id,
                        name,
                        definition);
            }
        });*/
    }
}
