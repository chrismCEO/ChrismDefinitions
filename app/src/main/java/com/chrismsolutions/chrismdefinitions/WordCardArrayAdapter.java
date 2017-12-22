package com.chrismsolutions.chrismdefinitions;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Christian Myrvold on 25.11.2017.
 */

public class WordCardArrayAdapter extends ArrayAdapter<Word> implements Serializable {
    private static final String LOG_TAG = WordCardArrayAdapter.class.getName();
    private ArrayList<Word> mWords;
    private Context mContext;
    int id = 0;

    WordCardArrayAdapter(@NonNull Context context, ArrayList<Word> words)
    {
        super(context, 0, words);
        mWords = words;
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        View listWordView = convertView;
        View listWordViewBack = null;
        Word word = mWords.get(position);
        boolean initialized = false;

        Log.i(LOG_TAG, "Position: " + position);

        if (listWordView == null)
        {
            listWordView = LayoutInflater.from(getContext()).inflate(
                    R.layout.fragment_card_front,
                    parent,
                    false);

            listWordViewBack = LayoutInflater.from(getContext()).inflate(
                    R.layout.fragment_card_back,
                    parent,
                    false);

            initialized = true;

            id += 2;
            word.id = id;

            listWordView.setTag(word);
        }

        Log.i(LOG_TAG, "Setting values for word ID: " + word.id);

        if (initialized)
        {
            word.cardViewFront = (CardView) listWordView;
            word.cardViewBack = (CardView) listWordViewBack;
        }
        else
        {
            if (word.showingBack)
            {
                word.cardViewBack = (CardView)listWordView;

                if (word.cardViewFront == null)
                {
                    word.cardViewFront = (CardView) LayoutInflater.from(getContext()).inflate(
                            R.layout.fragment_card_front,
                            parent,
                            false);
                }
            }
            else
            {
                word.cardViewFront = (CardView) LayoutInflater.from(getContext()).inflate(
                        R.layout.fragment_card_front,
                        parent,
                        false);

                if (word.cardViewBack == null)
                {
                    word.cardViewBack = (CardView) LayoutInflater.from(getContext()).inflate(
                            R.layout.fragment_card_back,
                            parent,
                            false);
                }
            }
        }

        word.setValues(listWordView);

        return listWordView;
    }

    ArrayList<Word> getWordList()
    {
        return mWords;
    }
}
