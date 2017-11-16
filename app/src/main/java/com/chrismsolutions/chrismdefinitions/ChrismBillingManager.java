package com.chrismsolutions.chrismdefinitions;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.BillingResponse;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;

import java.util.List;

/**
 * Created by Christian Myrvold on 16.11.2017.
 */

public class ChrismBillingManager implements PurchasesUpdatedListener
{
    private static final String LOG_TAG = ChrismBillingManager.class.getName();
    private final Activity mActivity;
    private final BillingUpdatesListener mBillingUpdatesListener;
    private final Object mBillingClient;
    private boolean mIsServiceConnected;
    private int mBillingResponseCode;

    public interface BillingUpdatesListener
    {
        void onBillingClientSetupFinished();
        void onConsumeFinished(String token, @BillingResponse int result);
        void onPurchasesUpdated(List<Purchase> purchases);
    }

    public ChrismBillingManager(Activity activity, final BillingUpdatesListener updatesListener)
    {
        mActivity = activity;
        mBillingUpdatesListener = updatesListener;
        mBillingClient = new BillingClient.newBuilder(mActivity).setListener(this).build();

        startServiceConnection(new Runnable()
        {
            @Override
            public void run()
            {
                mBillingUpdatesListener.onBillingClientSetupFinished();
                queryPurchases();
            }
        });
    }

    public void startServiceConnection(final Runnable executeOnSuccess)
    {
        mBillingClient.startConnection(new BillingUpdatesListener()
        {
            @Override
            public void onBillingClientSetupFinished(@BillingResponse int billingResponseCode)
            {
                Log.d(LOG_TAG, "Setup finished. Response code: " + billingResponseCode);

                if (billingResponseCode == BillingResponse.OK)
                {
                    mIsServiceConnected = true;

                    if (executeOnSuccess != null)
                    {
                        executeOnSuccess.run();
                    }
                }
                mBillingResponseCode = billingResponseCode;
            }

            @Override
            public void onBillingServiceDisconnected()
            {
                mIsServiceConnected = false;
            }
        });
    }

    private void queryPurchases()
    {
    }

    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases)
    {
    }
}
