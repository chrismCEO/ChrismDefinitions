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
    private HashMap<Integer, WordViewHolder> viewHolders;
    private Context mContext;
    int id = 0;

    WordCardArrayAdapter(@NonNull Context context, ArrayList<Word> words)
    {
        super(context, 0, words);
        mWords = words;
        mContext = context;
        viewHolders = new HashMap<>();

        //TODO: Weighted randomness, show cards with higher success rate less than those with lower rate
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        View listWordView = convertView;
        View listWordViewBack = null;
        WordViewHolder viewHolder;
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
            //word.setFirstValues(listWordView);

            /*viewHolder = new WordViewHolder(
                    listWordView,
                    (WordCardFlipActivity) mContext,
                    id,
                    word);*/

            listWordView.setTag(word);//viewHolder);
            //viewHolders.put(position, viewHolder);
        }
        else
        {
            //word = (Word) listWordView.getTag();
            //reloadViews = true;
            /*if (word.showingBack)
            {
                listWordView = LayoutInflater.from(getContext()).inflate(
                        R.layout.fragment_card_back,
                        parent,
                        false);
            }
            else
            {
                listWordView = LayoutInflater.from(getContext()).inflate(
                        R.layout.fragment_card_front,
                        parent,
                        false);
            }*/
        }

        //viewHolder.setValues();

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
                        false);//(CardView)listWordView;

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

    public ArrayList<Word> getWordList()
    {
        return mWords;
    }

    static class WordViewHolder extends RecyclerView.ViewHolder
    {
        TextView wordName;
        TextView wordDefinition;
        CardView cardViewFront, cardViewBack;
        ImageView wrongAnswer, correctAnswer;
        int id;
        boolean showingBack = false;
        WordCardFlipActivity flipActivity;
        String wordNameText, wordDefinitionText;
        Word word;
        FragmentManager fragmentManager;
        WordCardFrontFragment frontFragment;
        WordCardBackFragment backFragment;

        WordViewHolder(View itemView,
                       WordCardFlipActivity mFlipActivity,
                       int mId,
                       Word mWord)
        {
            super(itemView);
            word = mWord;
            flipActivity = mFlipActivity;
            id = mId;
            frontFragment = new WordCardFrontFragment();
            backFragment = new WordCardBackFragment();
            frontFragment.viewHolder = this;
            backFragment.viewHolder = this;

            wordName = (TextView) itemView.findViewById(R.id.word_name_card);
            wordName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    flipCard();
                }
            });
            word.cardViewFront = (CardView) itemView;//wordView.findViewById(R.id.container);
            //word.cardView.setId(id);
            /*cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    flipCard();
                }
            });*/
            //Log.i(WordViewHolder.class.getName(), "ViewHolder created with ID: " + cardView.getId());

            fragmentManager = flipActivity.getFragmentManager();
            //Commenting this out removes the border on the front card
            //fragmentManager = FragmentManager.
            //flipActivity.getFragmentManager()
            /*fragmentManager
                    .beginTransaction()
                    .add(cardView.getId(), frontFragment)
                    .commit();*/

            Log.i(LOG_TAG, "Front fragment added");
        }

        void flipCard()
        {
            if (word.showingBack)
            {
                word.showingBack = false;
                fragmentManager.popBackStack();//flipActivity.getFragmentManager().popBackStack();
            }
            else
            {
                word.showingBack = true;
                showFragment(backFragment);
            }
        }

        void showFragment(Fragment fragment)
        {
            fragmentManager//flipActivity.getFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.animator.card_flip_right_in,
                            R.animator.card_flip_right_out,
                            R.animator.card_flip_left_in,
                            R.animator.card_flip_left_out)
                    //.replace(word.cardView.getId(), fragment)
                    .addToBackStack(null)
                    .commit();
        }

        void setValues()
        {
            //word = mWord;
            //word.fragmentManager = fragmentManager;
            wordNameText = word.name;
            wordDefinitionText = word.definition;
            setTextViewValues();
        }

        void setTextViewValues()
        {
            if (!word.showingBack)
            {
                wordName.setText(wordNameText);
            }
            else
            {
                wordDefinition.setText(wordDefinitionText);
                showFragment(backFragment);
            }
        }

        private void setCardId()
        {
            //word.cardView.setId(id);
        }

        /**
         * A fragment representing the front of the card
         */
        public static class WordCardFrontFragment extends Fragment
        {
            WordViewHolder viewHolder;
            Word word;

            public WordCardFrontFragment()
            {
            }

            @Nullable
            @Override
            public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
            {
                View view = word.cardViewFront;//inflater.inflate(R.layout.fragment_card_front, container, false);
                word.setValues(view);
                /*
                viewHolder.wordName = (TextView) view.findViewById(R.id.word_name_card);
                viewHolder.setTextViewValues();
                viewHolder.word.cardView = (CardView)view;
                viewHolder.setCardId();*/
                return view;
            }
        }

        /**
         * A fragment representing the back of the card
         */
        public static class WordCardBackFragment extends Fragment
        {
            WordViewHolder viewHolder;
            Word word;

            public WordCardBackFragment()
            {
            }

            @Nullable
            @Override
            public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
            {
                final ImageView wrongAnswer, correctAnswer;
                View view = word.cardViewBack;//inflater.inflate(R.layout.fragment_card_back, container, false);
                word.setValues(view);

                /*TextView wordDefinition = (TextView) view.findViewById(R.id.word_definition_card);
                wordDefinition.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewHolder.flipCard();
                    }
                });
                viewHolder.wordDefinition = wordDefinition;

                viewHolder.setTextViewValues();
                viewHolder.word.cardView = (CardView)view;
                viewHolder.setCardId();

                wrongAnswer = (ImageView)view.findViewById(R.id.card_wrong_definition);
                viewHolder.wrongAnswer = wrongAnswer;

                correctAnswer = (ImageView) view.findViewById(R.id.card_correct_definition);
                correctAnswer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        correctAnswer.setColorFilter(ContextCompat.getColor(viewHolder.flipActivity, R.color.success_green));
                        wrongAnswer.setColorFilter(ContextCompat.getColor(viewHolder.flipActivity, R.color.suspend));
                        viewHolder.word.correctPoint = 1;
                        viewHolder.word.wrongPoint = 0;
                        viewHolder.flipActivity.enableFinishButton();
                    }
                });
                viewHolder.correctAnswer = correctAnswer;

                wrongAnswer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        wrongAnswer.setColorFilter(ContextCompat.getColor(viewHolder.flipActivity, R.color.fail_red));
                        correctAnswer.setColorFilter(ContextCompat.getColor(viewHolder.flipActivity, R.color.suspend));
                        viewHolder.word.wrongPoint = 1;
                        viewHolder.word.correctPoint = 0;
                        viewHolder.flipActivity.enableFinishButton();
                    }
                });

                if (viewHolder.word.correctPoint == 1)
                {
                    correctAnswer.setColorFilter(ContextCompat.getColor(viewHolder.flipActivity, R.color.success_green));
                    wrongAnswer.setColorFilter(ContextCompat.getColor(viewHolder.flipActivity, R.color.suspend));
                }
                else if (viewHolder.word.wrongPoint == 1)
                {
                    wrongAnswer.setColorFilter(ContextCompat.getColor(viewHolder.flipActivity, R.color.fail_red));
                    correctAnswer.setColorFilter(ContextCompat.getColor(viewHolder.flipActivity, R.color.suspend));
                }*/

                return view;
            }
        }
    }
}
