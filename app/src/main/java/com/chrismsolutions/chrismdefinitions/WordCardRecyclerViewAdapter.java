package com.chrismsolutions.chrismdefinitions;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Christian Myrvold on 23.11.2017.
 */
/*
public class WordCardRecyclerViewAdapter extends RecyclerView.Adapter<WordCardRecyclerViewAdapter.WordViewHolder>
{
    private static final String LOG_TAG = WordViewHolder.class.getName();
    //List<Word> words;
    Context context;
    private WordCardFlipActivity flipActivity;
    int id = 0;

    WordCardRecyclerViewAdapter(List<Word> mWords, Context mContext)
    {
        words = mWords;
        context = mContext;
        flipActivity = (WordCardFlipActivity)context;
    }

    @Override
    public WordViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_card_front, parent, false);
        id++;
        final WordViewHolder wordViewHolder = new WordViewHolder(view, flipActivity, id);
        return wordViewHolder;
    }

    @Override
    public void onBindViewHolder(WordViewHolder holder, int position)
    {
        //holder.wordName.setText(words.get(position).name);
        //holder.wordDefinition.setText(words.get(position).definition);
        holder.word = words.get(position);
        holder.setValues();
    }

    @Override
    public int getItemCount()
    {
        return words.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static class WordViewHolder extends ViewHolder
    {
        TextView wordName;
        TextView wordDefinition;
        boolean showingBack = false;
        WordCardFlipActivity flipActivity;
        String wordNameText, wordDefinitionText;
        Word word;
        CardView mCardView;
        FragmentManager fragmentManager;
        WordCardFrontFragment frontFragment;
        WordCardBackFragment backFragment;
        boolean done = false;

        public WordViewHolder(View cardView,
                              WordCardFlipActivity mFlipActivity,
                              int id)
        {
            super(cardView);
            mCardView = (CardView) cardView;
            flipActivity = mFlipActivity;
            frontFragment = new WordCardFrontFragment();
            backFragment = new WordCardBackFragment();
            frontFragment.viewHolder = this;
            backFragment.viewHolder = this;

            wordName = (TextView) cardView.findViewById(R.id.word_name_card);
            //cardView = (CardView) wordView.findViewById(R.id.container);
            mCardView.setId(id);
            mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    flipCard();
                }
            });
            Log.i(WordViewHolder.class.getName(), "ViewHolder created with ID: "+cardView.getId());

            if (id > 4) {
                try {
                    synchronized (this) {
                        wait(2000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            fragmentManager = flipActivity.getFragmentManager();
            fragmentManager.beginTransaction()
                    .add(mCardView.getId(), frontFragment)
                    .commit();

            Log.i(LOG_TAG, "Front fragment added");
        }

        public void flipCard()
        {
            if (showingBack)
            {
                fragmentManager.popBackStack();
                showingBack = false;
            }
            else
            {
                showingBack = true;

                fragmentManager
                        .beginTransaction()
                        .setCustomAnimations(
                                R.animator.card_flip_right_in,
                                R.animator.card_flip_right_out,
                                R.animator.card_flip_left_in,
                                R.animator.card_flip_left_out)
                        .replace(mCardView.getId(), backFragment)
                        .addToBackStack(null)
                        .commit();
            }
        }

        public void setValues()
        {
            wordNameText = word.name;
            wordDefinitionText = word.definition;
            setTextViewValues();
        }

        public void setTextViewValues()
        {
            if (!showingBack)
            {
                wordName.setText(wordNameText);
            }
            else
            {
                wordDefinition.setText(wordDefinitionText);
            }
        }

        public void setDone(boolean done) {
            this.done = done;
        }
*/
        /**
         * A fragment representing the front of the card
         */
        /*
        public static class WordCardFrontFragment extends Fragment
        {
            WordViewHolder viewHolder;

            public WordCardFrontFragment()
            {
            }

            @Nullable
            @Override
            public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
            {
                Log.i(LOG_TAG, "View about to be created ID: " + viewHolder.mCardView.getId());
                View view = inflater.inflate(R.layout.fragment_card_front, container, false);
                viewHolder.wordName = (TextView) view.findViewById(R.id.word_name_card);
                viewHolder.setTextViewValues();
                Log.i(LOG_TAG, "View created");
                return view;
            }

            @Override
            public void onStart() {
                super.onStart();
                Log.i(LOG_TAG, "View started with ID: " + viewHolder.mCardView.getId());
            }
        }
*/
        /**
         * A fragment representing the back of the card
         */
        /*
        public static class WordCardBackFragment extends Fragment
        {
            WordViewHolder viewHolder;

            public WordCardBackFragment()
            {

            }

            @Nullable
            @Override
            public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
            {
                View view = inflater.inflate(R.layout.fragment_card_back, container, false);
                viewHolder.wordDefinition = (TextView) view.findViewById(R.id.word_definition_card);
                viewHolder.setTextViewValues();
                return view;
            }
        }
        */

    //}

//}
