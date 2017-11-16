package com.chrismsolutions.chrismdefinitions;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;

import com.android.billingclient.api.Purchase;
import com.chrismsolutions.chrismdefinitions.ChrismBillingManager.BillingUpdatesListener;

import java.util.List;

/**
 * Created by Christian Myrvold on 16.11.2017.
 */

public class MainViewController extends FragmentActivity implements BillingProvider
{

    private final Activity mActivity;

    public MainViewController (Activity activity)
    {
        mActivity = activity;
    }

    private class UpdateListener implements BillingUpdatesListener
    {
        @Override
        public void onBillingClientSetupFinished() {
            mActivity.onBillingManagerSetupFinished();
        }

        @Override
        public void onConsumeFinished(String token, int result) {

        }

        @Override
        public void onPurchasesUpdated(List<Purchase> purchases) {

        }
    }


}
