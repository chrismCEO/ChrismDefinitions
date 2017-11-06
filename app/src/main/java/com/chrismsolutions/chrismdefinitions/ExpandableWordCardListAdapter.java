package com.chrismsolutions.chrismdefinitions;

import android.content.Context;
import android.graphics.Typeface;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.chrismsolutions.chrismdefinitions.data.DefinitionProvider;
import com.chrismsolutions.chrismdefinitions.data.DefinitionsContract.DefinitionsEntry;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Christian Myrvold on 01.11.2017.
 */

public class ExpandableWordCardListAdapter extends BaseExpandableListAdapter {

    private final Context mContext;
    private List<String> mList;
    private HashMap<String, List<String>> mListHashMap;
    private HashMap<Integer, Integer> mListHashMapIds;
    private int folderId;

    public ExpandableWordCardListAdapter(Context context,
                                         List<String> list,
                                         HashMap<String, List<String>> listHashMap,
                                         HashMap<Integer, Integer> listHashMapIds,
                                         int id)
    {
        mContext = context;
        mList = list;
        mListHashMap = listHashMap;
        mListHashMapIds = listHashMapIds;
        folderId = id;
    }

    @Override
    public int getGroupCount() {
        return mList.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return mListHashMap.get(mList.get(i)).size();
    }

    @Override
    public Object getGroup(int i) {
        return this.mList.get(i);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mListHashMap.get(mList.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean b, View view, ViewGroup viewGroup) {
        String headerTitle = (String) getGroup(groupPosition);

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_group, null);
        }

        TextView listHeader = (TextView) view.findViewById(R.id.listHeader);
        listHeader.setTypeface(null, Typeface.BOLD);
        listHeader.setText(headerTitle);

        return view;
    }

    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup viewGroup) {
        final String childText = (String) getChild(groupPosition, childPosition);
        final int groupPositionLocal = groupPosition;

        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.list_item_word_cards, null);
        }

        TextView textListChild = (TextView) view.findViewById(R.id.word_card_text);
        textListChild.setText(childText);

        final int wordCardId = mListHashMapIds.get(groupPosition);

        ImageView imageEdit = (ImageView) view.findViewById(R.id.edit_word_card);
        imageEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle arguments = new Bundle();
                arguments.putBoolean(WordCardDialogFragment.DIALOG_WORD_CARD_EDIT, true);
                arguments.putInt(WordCardDialogFragment.DIALOG_WORD_CARD_ID, wordCardId);
                arguments.putString(WordCardDialogFragment.DIALOG_WORD_CARD_NAME, (String) getGroup(groupPosition));
                arguments.putString(WordCardDialogFragment.DIALOG_WORD_CARD_DEF, childText);

                FragmentActivity fragmentActivity = (FragmentActivity)mContext;
                WordCardDialogFragment fragment = new WordCardDialogFragment();
                fragment.setArguments(arguments);
                fragment.show(fragmentActivity.getSupportFragmentManager(), WordCardDialogFragment.DIALOG_TAG);
            }
        });

        ImageView imageDelete = (ImageView) view.findViewById(R.id.delete_word_card);
        imageDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelper.deleteWordCardConfirmationDialog(
                        mContext,
                        wordCardId,
                        folderId,
                        (String) getGroup(groupPosition));
            }
        });

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    public void setNewItems(
            List<String> listDataHeader,
            HashMap<String, List<String>> listChildData,
            HashMap<Integer, Integer> listDataChildIds)
    {
        mList = listDataHeader;
        mListHashMap = listChildData;
        mListHashMapIds = listDataChildIds;
        notifyDataSetChanged();
    }
}
