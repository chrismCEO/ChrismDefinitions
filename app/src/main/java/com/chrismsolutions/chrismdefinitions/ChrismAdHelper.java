package com.chrismsolutions.chrismdefinitions;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

/**
 * Created by Christian Myrvold on 08.11.2017.
 */

public class ChrismAdHelper
{
    private static boolean TEST_UNIT = false;

    /**
     * Load the ad programmatically. We have to do it this way if we want to be able to easily switch
     * out the unit ID to live ads or test ads.
     * @param context
     * @param view
     */
    public static void createAd(Context context, RelativeLayout view)
    {
        boolean isWordCardActiviy = context.getClass() == WordCardActivity.class;

        if (showAd(context))
        {
            //AdView mAdView = (AdView) view;
            AdView mAdView = new AdView(context);
            RelativeLayout.LayoutParams adParams = new
                    RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);

            TypedValue typedValue = new TypedValue();
            if (!isWordCardActiviy && context.getTheme().resolveAttribute(R.attr.actionBarSize, typedValue, true))
            {
                int marginTop =  TypedValue.complexToDimensionPixelSize(typedValue.data, context.getResources().getDisplayMetrics());
                adParams.setMargins(0, marginTop, 0, 0);
            }
            adParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            adParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                adParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mAdView.setId(View.generateViewId());
            }

            mAdView.setLayoutParams(adParams);
            mAdView.setAdSize(AdSize.SMART_BANNER);

            //Set the list to be below the ad
            LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.contentMainLayout);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(//(RelativeLayout.LayoutParams) linearLayout.getLayoutParams();
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);

            layoutParams.addRule(RelativeLayout.BELOW, mAdView.getId());

            linearLayout.setLayoutParams(layoutParams);


            AdRequest adRequest = new AdRequest.Builder().build();

            //Insert correct adUnitId depending on if this is a test unit
            if (isTestDevice(context))
            {
                mAdView.setAdUnitId(context.getString(R.string.adBannerUnitIdTest));
            }
            else
            {
                mAdView.setAdUnitId(context.getString(R.string.adBannerUnitId));
            }

            view.addView(mAdView);

            mAdView.loadAd(adRequest);
        }
    }

    /**
     * Check if the ads are to be shown. Ads need to be suppressed if we find that the user has
     * payed. The check will be done against Google Payments.
     * @return true if the user has not payed to suppress ads, otherwise false
     */
    public static boolean showAd(Context context)
    {
        return true;
    }

    public static boolean isTestDevice(Context context)
    {
        return Boolean.valueOf(Settings.System.getString(context.getContentResolver(), "firebase.test.lab"))
                || TEST_UNIT;
    }
}
