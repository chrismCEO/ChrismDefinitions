package com.chrismsolutions.chrismdefinitions;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chrismsolutions.chrismdefinitions.data.DefinitionsContract.DefinitionsEntry;

/**
 * Created by Christian Myrvold on 17.12.2017.
 */

public class WordCardResultAdapter extends CursorAdapter
{
    WordCardResultAdapter(Context context, Cursor cursor)
    {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(
                R.layout.list_item_results,
                parent,
                false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        TextView wordName = view.findViewById(R.id.result_word_name);
        wordName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_WORD_CARD_NAME)));

        int correctTotal = cursor.getInt(cursor.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_WORD_CARD_CORRECT_TOTAL));
        int correctLast = cursor.getInt(cursor.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_WORD_CARD_CORRECT_LAST));
        int wrongTotal = cursor.getInt(cursor.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_WORD_CARD_WRONG_TOTAL));
        int wrongLast = cursor.getInt(cursor.getColumnIndexOrThrow(DefinitionsEntry.COLUMN_WORD_CARD_WRONG_LAST));
        int totalAll = correctTotal + wrongTotal;
        int totalLast = correctLast + wrongLast;
        double percentageTotal = ((double)correctTotal / (double) totalAll);
        int percentageTotalInt = (int) (percentageTotal * 100);
        double percentageLast = ((double) correctLast / (double) totalLast);
        int percentageLastInt = (int) (percentageLast * 100);

        TextView percentageTotalView = view.findViewById(R.id.result_percentage_total);
        percentageTotalView.setText(context.getString(R.string.percentage, String.valueOf(percentageTotalInt)));

        TextView percentageLastView = view.findViewById(R.id.result_percentage_last);
        percentageLastView.setText(context.getString(R.string.percentage, String.valueOf(percentageLastInt)));

        ImageView imageView = view.findViewById(R.id.result_smiley);

        imageView.setImageResource(FinishTestActivity.setDrawableTopResult(percentageLastInt));
        imageView.setColorFilter(R.color.colorSecondaryText);
    }
}
