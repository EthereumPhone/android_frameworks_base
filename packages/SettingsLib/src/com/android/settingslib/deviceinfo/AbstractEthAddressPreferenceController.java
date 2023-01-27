/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settingslib.deviceinfo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settingslib.R;
import com.android.settingslib.core.lifecycle.Lifecycle;

import java.lang.reflect.*;

/**
 * Preference controller for ETH address
 */
public abstract class AbstractEthAddressPreferenceController
        extends AbstractConnectivityPreferenceController {

    @VisibleForTesting
    static final String KEY_ETH_ADDRESS = "eth_address";

    private static final String[] CONNECTIVITY_INTENTS = {
            ConnectivityManager.CONNECTIVITY_ACTION,
            WifiManager.ACTION_LINK_CONFIGURATION_CHANGED,
            WifiManager.NETWORK_STATE_CHANGED_ACTION,
    };

    private Preference mEthAddress;
    private final Object walletManager;
    private Class walletProxy;
    private Method getAddress;
    private Method createSession;
    private Method hasBeenFulfilled;

    public AbstractEthAddressPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, lifecycle);
        walletManager = context.getSystemService("wallet");
        try {
            walletProxy = Class.forName("android.os.WalletProxy");
        } catch (ClassNotFoundException classNotFoundException) {
            classNotFoundException.printStackTrace();
            walletProxy = null;
        }
        getAddress = walletProxy.getDeclaredMethods()[2];
        createSession = walletProxy.getDeclaredMethods()[1];
        hasBeenFulfilled = walletProxy.getDeclaredMethods()[4];
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_ETH_ADDRESS;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            mEthAddress = screen.findPreference(KEY_ETH_ADDRESS);
            updateConnectivity();
        }
    }

    @Override
    protected String[] getConnectivityIntents() {
        return CONNECTIVITY_INTENTS;
    }

    @SuppressLint("HardwareIds")
    @Override
    protected void updateConnectivity() {
        if (walletManager == null) {
            mEthAddress.setSummary(R.string.status_unavailable);
        } else {

            final Preference mEthAddressF = mEthAddress;
            final Method createSessionF = createSession;
            final Method getAddressF = getAddress;

            try {
                String sessionId = (String) createSessionF.invoke(walletManager);
                String requestId = (String) getAddressF.invoke(walletManager, sessionId);

                Thread.sleep(100);

                while (hasBeenFulfilled.invoke(walletManager, requestId).equals("notfulfilled")) {
                }

                String ethAddress = (String) hasBeenFulfilled.invoke(walletManager, requestId);
                mEthAddressF.setSummary(ethAddress);
            } catch (Exception exception) {
                exception.printStackTrace();
                mEthAddressF.setSummary(R.string.status_unavailable);
            }

        }
    }
}
