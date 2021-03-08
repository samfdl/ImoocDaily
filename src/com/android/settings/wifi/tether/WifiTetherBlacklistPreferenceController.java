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

package com.android.settings.wifi.tether;

import android.content.Context;
import android.net.wifi.WifiDevice;
import android.net.wifi.WifiManager;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import com.android.settings.R;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

import java.util.ArrayList;
import java.util.List;

public class WifiTetherBlacklistPreferenceController extends WifiTetherBasePreferenceController
        implements OnStart, OnStop {

    private static final String PREF_KEY = "ap_device_list";
    private static final String TAG = "WifiTetherConnectedDevicesPreferenceController";
    private static final boolean DBG = false;

    private List<String> mConnectedNameList = new ArrayList<String>();
    private List<String> mConnectedAddressList = new ArrayList<String>();
    private List<WifiDevice> mTetherConnectedDeviceList = new ArrayList<WifiDevice>();
    private PreferenceCategory mWifiApListPrefCategory;
    private Context mContext;
    private WifiTetherSoftApManager mWifiTetherSoftApManager;
    private WifiManager mWifiManager;

    public WifiTetherBlacklistPreferenceController(Context context,
                                                   OnTetherConfigUpdateListener listener) {
        super(context, listener);
        mContext = context;
        mWifiManager = (WifiManager) mContext.getSystemService(WifiManager.class);
        initWifiTetherSoftApManager();
    }

    @Override
    public void onStart() {
        if (mPreference != null) {
            if (mWifiTetherSoftApManager != null) {
                if (DBG) Log.d(TAG, "registerSoftApCallback");
                mWifiTetherSoftApManager.registerSoftApCallback();
            }
        }
    }

    @Override
    public void onStop() {
        if (mPreference != null) {
            if (mWifiTetherSoftApManager != null) {
                if (DBG) Log.d(TAG, "unRegisterSoftApCallback");
                mWifiTetherSoftApManager.unRegisterSoftApCallback();
            }
        }
    }

    private void initWifiTetherSoftApManager() {
        mWifiTetherSoftApManager = new WifiTetherSoftApManager(mWifiManager,
                new WifiTetherSoftApManager.WifiTetherSoftApCallback() {
                    @Override
                    public void onStateChanged(int state, int failureReason) {
                        if (DBG) Log.d(TAG, "onStateChanged: " +state);
                        updateConnectedDevices();
                    }

                    @Override
                    public void onNumClientsChanged(int numClients) {
                        if (DBG) Log.d(TAG, "onNumClientsChanged: " +numClients);
                        try {
                            Thread.sleep(30);
                        } catch (InterruptedException e) {
                            Log.e(TAG, ""+e);
                        }
                        updateConnectedDevices();
                    }
                });
    }

    @Override
    public void updateDisplay() {
        mWifiApListPrefCategory = (PreferenceCategory) mPreference;
        updateConnectedDevices();
    }

    @Override
    public String getPreferenceKey() {
        return PREF_KEY;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }

    private void updateConnectedDevices() {
        mTetherConnectedDeviceList = mWifiManager.getConnectedStations();
        if (mTetherConnectedDeviceList == null || mTetherConnectedDeviceList.size() == 0) {
            if (DBG) Log.d(TAG, "ConnectedCount = 0");
            mWifiApListPrefCategory.removeAll();
            return;
        }
        mConnectedNameList.clear();
        mConnectedAddressList.clear();
        for (int i = 0; i < mTetherConnectedDeviceList.size(); i++) {
            WifiDevice device = mTetherConnectedDeviceList.get(i);
            mConnectedNameList.add(device.deviceName != null ? device.deviceName : "");
            mConnectedAddressList.add(device.deviceIpAddress+"\n"+device.deviceAddress);
        }
        mWifiApListPrefCategory.removeAll();
        for (int index = 0; index < mConnectedAddressList.size(); ++index) {
            if (DBG) Log.d(TAG, "Connected device address - " + mConnectedAddressList.get(index));
            Preference pref = new Preference(mContext);
            if (mConnectedNameList != null && !mConnectedNameList.get(index).isEmpty()) {
                if (DBG) Log.d(TAG, "Connected device name - " + mConnectedNameList.get(index));
                pref.setTitle(mConnectedNameList.get(index));
            } else {
                pref.setTitle(R.string.wifiap_default_device_name);
            }
            pref.setSummary(mConnectedAddressList.get(index));
            mWifiApListPrefCategory.addPreference(pref);
        }
    }
}
