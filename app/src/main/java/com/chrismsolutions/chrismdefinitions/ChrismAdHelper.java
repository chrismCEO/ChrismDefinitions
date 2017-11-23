package com.chrismsolutions.chrismdefinitions;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.chrismsolutions.chrismdefinitions.billingUtil.IabHelper;
import com.chrismsolutions.chrismdefinitions.billingUtil.IabResult;
import com.chrismsolutions.chrismdefinitions.billingUtil.Inventory;
import com.chrismsolutions.chrismdefinitions.billingUtil.Purchase;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.io.Serializable;

/**
 * Created by Christian Myrvold on 08.11.2017.
 */

public class ChrismAdHelper implements Serializable
{
    private static final int REQUEST_CODE = 10001;
    private static final String LOG_TAG = ChrismAdHelper.class.getName();
    public static final String ADHELPER_CLASS = LOG_TAG;
    private static boolean TEST_UNIT = true;
    private Context mContext;
    private IabHelper mHelper;
    private boolean mCallback;
    private boolean mHelperSetup = false;
    private static String SKU_IN_APP_PURCHASE;
    private boolean isPremiumUser = false;

    public ChrismAdHelper(Context context, boolean initHelper, boolean callback)
    {
        mContext = context;
        mCallback = callback;

        if (isTestDevice())
        {
            SKU_IN_APP_PURCHASE = mContext.getString(R.string.premium_product_id_test_purchased);
        }
        else
        {
            SKU_IN_APP_PURCHASE = mContext.getString(R.string.premium_product_id);
        }

        if (initHelper)
        {
            mHelper = createIabHelper();
        }
    }

    private IabHelper createIabHelper()
    {
        String base64EncodedPublicKey = mContext.getResources().getString(R.string.billing_encoded_public_key);
        mHelper = new IabHelper(mContext, base64EncodedPublicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener()
        {
            @Override
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess())
                {
                    mHelperSetup = false;
                    Log.e(LOG_TAG, "IabHelper not setup");
                }
                else
                {
                    mHelperSetup = true;
                    try {
                        mHelper.queryInventoryAsync(mInventoryListener);

                    } catch (IabHelper.IabAsyncInProgressException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        return mHelper;
    }

    public static ChrismAdHelper createAdStatic(Context context,
                                                RelativeLayout relativeLayout)
    {
        ChrismAdHelper adHelper = new ChrismAdHelper(context, false, false);
        adHelper.createAd(relativeLayout);
        return adHelper;
    }

    /**
     * Load the ad programmatically. We have to do it this way if we want to be able to easily switch
     * out the unit ID to live ads or test ads.
     * @param view
     */
    public void createAd(RelativeLayout view)
    {
        boolean isWordCardActiviy = mContext.getClass() == WordCardActivity.class;

        if (showAd())
        {
            AdView mAdView = new AdView(mContext);
            RelativeLayout.LayoutParams adParams = new
                    RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);

            TypedValue typedValue = new TypedValue();
            if (!isWordCardActiviy && mContext.getTheme().resolveAttribute(R.attr.actionBarSize, typedValue, true))
            {
                int marginTop =  TypedValue.complexToDimensionPixelSize(typedValue.data, mContext.getResources().getDisplayMetrics());
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
            if (isTestDevice())
            {
                mAdView.setAdUnitId(mContext.getString(R.string.adBannerUnitIdTest));
            }
            else
            {
                mAdView.setAdUnitId(mContext.getString(R.string.adBannerUnitId));
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
    public boolean showAd()
    {
        return !isPremiumUser;
    }

    private IabHelper.QueryInventoryFinishedListener mInventoryListener =
            new IabHelper.QueryInventoryFinishedListener() {
                @Override
                public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                    if (result.isFailure())
                    {
                        Log.d(LOG_TAG, "Error getting purchase result: " + result);
                    }
                    else
                    {
                        isPremiumUser = inv.hasPurchase(SKU_IN_APP_PURCHASE);
                        /*try {
                            mHelper.consumeAsync(inv.getPurchase(SKU_IN_APP_PURCHASE), null);
                        } catch (IabHelper.IabAsyncInProgressException e) {
                            e.printStackTrace();
                        }*/
                    }
                    refreshCallingActivity();
                }
            };


    private void refreshCallingActivity()
    {
        if (!mCallback)
        {
            MainActivity mainActivity = (MainActivity) mContext;
            mainActivity.changeDesign();
        }
    }

    private boolean isTestDevice()
    {
        return Boolean.valueOf(Settings.System.getString(mContext.getContentResolver(), "firebase.test.lab"))
                || TEST_UNIT;
    }

    private IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener =
            new IabHelper.OnIabPurchaseFinishedListener() {
                @Override
                public void onIabPurchaseFinished(IabResult result, Purchase info) {
                    if (result.isFailure())
                    {
                        Log.d(LOG_TAG, "Error purchasing: " + info);
                    }
                    else if (info.getSku().equals(SKU_IN_APP_PURCHASE))
                    {
                        isPremiumUser = true;
                    }
                    else
                    {
                        isPremiumUser = false;
                    }
                    refreshCallingActivity();
                }
            };

    public IabHelper getIabHelper()
    {
        return mHelper;
    }

    public boolean removeAds()
    {
        boolean result = false;
        if (mHelper != null)
        {
            MainActivity activity = (MainActivity)mContext;
            try {
                mHelper.launchPurchaseFlow(
                        activity,
                        SKU_IN_APP_PURCHASE,
                        REQUEST_CODE,
                        mPurchaseFinishedListener,
                        "");
                result = true;
                mCallback = false;
            } catch (IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            }
        }

        return result;
    }
}
