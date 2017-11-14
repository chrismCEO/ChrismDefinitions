package com.chrismsolutions.chrismdefinitions;

import android.content.Context;
import android.provider.Settings;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

/**
 * Created by Christian Myrvold on 08.11.2017.
 */

public class ChrismAdHelper
{
    /**
     * Load the ad to the given view.
     * @param context
     * @param view
     */
    public static void createFolderAd(Context context, View view)
    {
        AdView mAdViewFolder = (AdView) view;

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdViewFolder.loadAd(adRequest);
    }

    /**
     * Check if the ads are to be shown. Ads need to be suppressed if we find that the user has
     * payed. The check will be done against Google Payments.
     * @return true if the user has not payed to suppress ads, otherwise false
     */
    public static boolean showAd()
    {
        return false;
    }

    public boolean isTestDevice(Context context)
    {
        return Boolean.valueOf(Settings.System.getString(context.getContentResolver(), "firebase.test.lab"));
    }
}
