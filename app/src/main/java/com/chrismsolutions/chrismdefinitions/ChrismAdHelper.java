package com.chrismsolutions.chrismdefinitions;

import android.content.Context;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

/**
 * Created by zelda on 08.11.2017.
 */

public class ChrismAdHelper
{
    public static void createFolderAd(Context context, View view)
    {
        AdView mAdViewFolder = (AdView) view;

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdViewFolder.loadAd(adRequest);
    }

    public static boolean showAd()
    {
        return false;
    }
}
