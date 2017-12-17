package com.chrismsolutions.chrismdefinitions;

import android.app.Fragment;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Christian Myrvold on 17.12.2017.
 */

public class Word
        implements Parcelable
{
    public static final int TWENTY_PERCENT = 20;
    public static final int FORTY_PERCENT = 40;
    public static final int SIXTY_PERCENT = 60;
    public static final int EIGHTY_PERCENT = 80;

    private static final String LOG_TAG = Word.class.getName();
    private WordCardFlipActivity context;

    String name;
    String definition;
    boolean showingBack = false;
    int correctPoint, wrongPoint;
    CardView cardViewFront, cardViewBack;
    int id, backId;
    private WordCardArrayAdapter.WordViewHolder.WordCardFrontFragment frontFragment;
    private WordCardArrayAdapter.WordViewHolder.WordCardBackFragment backFragment;
    private TextView wordName;
    private TextView wordDefinition;
    private ImageView wrongAnswer;
    private ImageView correctAnswer;
    private ImageView correctImage, wrongImage;
    private int percentage;
    int dbID;

    public Word(String mName, String mDefinition, int mPercentage, int mDbId)
    {
        name = mName;
        definition = mDefinition;
        percentage = mPercentage;
        dbID = mDbId;
    }

    public void setContext(Context mContext)
    {
        context = (WordCardFlipActivity)mContext;
    }

    void setFirstValues(View view)
    {
        Log.i(LOG_TAG, "Setting first values for ID: " + id);

        frontFragment = new WordCardArrayAdapter.WordViewHolder.WordCardFrontFragment();
        backFragment = new WordCardArrayAdapter.WordViewHolder.WordCardBackFragment();

        frontFragment.word = this;
        backFragment.word = this;

        backId = id + 1;

        if (id == 0)
        {
            id = context.highestId+1;
            context.highestId++;
        }
    }

    void setValues(final View view)
    {
        if (frontFragment == null || backFragment == null)
        {
            setFirstValues(view);
        }

        if (id > context.highestId)
        {
            context.highestId = id;
        }

        if (showingBack)
        {
            cardViewBack = (CardView) view;
            cardViewBack.setId(backId);
        }
        else
        {
            cardViewFront = (CardView) view;
            cardViewFront.setId(id);
        }

        if (!showingBack)
        {
            if (wordName == null)
            {
                wordName = (TextView) cardViewFront.findViewById(R.id.word_name_card);
            }
            wordName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    flipCard();
                    Log.i(LOG_TAG, "Flipping card ID to back: " + id);
                    Log.i(LOG_TAG, "Word definition: " + definition);
                }
            });
            wordName.setText(name);

            //Set the images and colors
            //Correct or wrong answer image
            correctImage = (ImageView)cardViewFront.findViewById(R.id.card_correct_word);
            wrongImage = (ImageView)cardViewFront.findViewById(R.id.card_wrong_word);

            if (correctPoint == 1)
            {
                setCorrectColors(correctImage, wrongImage);
            }
            else if(wrongPoint == 1)
            {
                setWrongColors(correctImage, wrongImage);
            }
            else
            {
                //set the images to white, making them disappear
                setDefaultColors(correctImage, wrongImage);
            }

            //Set the smiley and percentage based on statistics
            ImageView statSmiley = (ImageView) cardViewFront.findViewById(R.id.statistic_smiley);
            TextView statPercentage = (TextView) cardViewFront.findViewById(R.id.statistic_percentage);

            statPercentage.setText(context.getString(R.string.percentage, percentage));
            statSmiley.setColorFilter(ContextCompat.getColor(context, R.color.colorSecondaryText));

            if (percentage < TWENTY_PERCENT)
            {
                statSmiley.setImageResource(R.mipmap.ic_sentiment_very_dissatisfied_white_48dp);
            }
            else if (percentage < FORTY_PERCENT)
            {
                statSmiley.setImageResource(R.mipmap.ic_sentiment_dissatisfied_white_48dp);
            }
            else if (percentage < SIXTY_PERCENT)
            {
                statSmiley.setImageResource(R.mipmap.ic_sentiment_neutral_white_48dp);
            }
            else if (percentage < EIGHTY_PERCENT)
            {
                statSmiley.setImageResource(R.mipmap.ic_sentiment_satisfied_white_48dp);
            }
            else
            {
                statSmiley.setImageResource(R.mipmap.ic_sentiment_very_satisfied_white_48dp);
            }
        }
        else
        {
            if (wordDefinition == null)
            {
                wordDefinition = (TextView) cardViewBack.findViewById(R.id.word_definition_card);
            }
            wordDefinition.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    flipCard();
                }
            });
            wordDefinition.setText(definition);

            if (wrongAnswer == null)
            {
                wrongAnswer = (ImageView) view.findViewById(R.id.card_wrong_definition);
                wrongAnswer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        setWrongColors(correctAnswer, wrongAnswer);
                        wrongPoint = 1;
                        correctPoint = 0;
                        context.enableFinishButton();
                        flipCard();
                        setValues(cardViewFront);
                    }
                });
            }

            if (correctAnswer == null)
            {
                correctAnswer = (ImageView) view.findViewById(R.id.card_correct_definition);
                correctAnswer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setCorrectColors(correctAnswer, wrongAnswer);
                        correctPoint = 1;
                        wrongPoint = 0;
                        context.enableFinishButton();
                        flipCard();
                        setValues(cardViewFront);
                    }
                });
            }

            if (correctPoint == 1)
            {
                correctAnswer.setColorFilter(ContextCompat.getColor(context, R.color.success_green));
                wrongAnswer.setColorFilter(ContextCompat.getColor(context, R.color.suspend));
            }
            else if (wrongPoint == 1)
            {
                wrongAnswer.setColorFilter(ContextCompat.getColor(context, R.color.fail_red));
                correctAnswer.setColorFilter(ContextCompat.getColor(context, R.color.suspend));
            }
        }
    }

    private void setDefaultColors(ImageView correct, ImageView wrong)
    {
        wrong.setColorFilter(ContextCompat.getColor(context, R.color.white));
        correct.setColorFilter(ContextCompat.getColor(context, R.color.white));
    }

    void setWrongColors(ImageView correct, ImageView wrong)
    {
        wrong.setColorFilter(ContextCompat.getColor(context, R.color.fail_red));
        correct.setColorFilter(ContextCompat.getColor(context, R.color.suspend));
    }

    void setCorrectColors(ImageView correct, ImageView wrong)
    {
        correct.setColorFilter(ContextCompat.getColor(context, R.color.success_green));
        wrong.setColorFilter(ContextCompat.getColor(context, R.color.suspend));
    }

    void flipCard()
    {
        if (showingBack)
        {
            context.getFragmentManager().popBackStack();
        }
        else
        {
            context.getFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.animator.card_flip_right_in,
                            R.animator.card_flip_right_out,
                            R.animator.card_flip_left_in,
                            R.animator.card_flip_left_out)
                    .replace(cardViewFront.getId(), backFragment)
                    .addToBackStack(null)
                    .commit();
        }

        showingBack = !showingBack;
    }

    void showFragment(Fragment fragment)
    {
        CardView cardView;

        if (showingBack)
        {
            cardView = cardViewBack;
        }
        else
        {
            cardView = cardViewFront;
        }

        context.getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.animator.card_flip_right_in,
                        R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in,
                        R.animator.card_flip_left_out)
                .replace(cardView.getId(), fragment)
                .addToBackStack(null)
                .commit();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
            /*
            name = mName;
            definition = mDefinition;
            percentage = mPercentage;
            dbID = mDbId;
             */
        dest.writeString(name);
        dest.writeString(definition);
        dest.writeInt(percentage);
        dest.writeInt(dbID);
        dest.writeInt(correctPoint);
        dest.writeInt(wrongPoint);
    }

    public static final Parcelable.Creator<Word> CREATOR = new Parcelable.Creator<Word>(){

        @Override
        public Word createFromParcel(Parcel source) {
            return new Word(source);
        }

        @Override
        public Word[] newArray(int size) {
            return new Word[size];
        }
    };

    private Word(Parcel in)
    {
        name = in.readString();
        definition = in.readString();
        percentage = in.readInt();
        dbID = in.readInt();
        correctPoint = in.readInt();
        wrongPoint = in.readInt();
    }
}
