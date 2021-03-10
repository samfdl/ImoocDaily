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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.settings.SettingsEnums;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.wifi.WifiMasterSwitchPreferenceController;
import com.android.settingslib.search.SearchIndexable;

import java.util.Arrays;
import java.util.List;

@SearchIndexable
public class WifiTetherBlacklist extends DashboardFragment {
    private static final String TAG = "WifiTetherBlacklist";

    private static final String PREF_KEY = "wifi_hotspot_black_list";

    private PreferenceCategory mWifiTetherBlacklistPrefCategory;

    private SharedPreferences mSharedPreferences;

    private String[] mAddressList;

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.SETTINGS_NETWORK_CATEGORY;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.wifi_tether_blacklist;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mWifiTetherBlacklistPrefCategory = getPreferenceScreen().findPreference(PREF_KEY);

        mSharedPreferences = getContext().getSharedPreferences("hotspot", Context.MODE_PRIVATE);
        System.out.println("mSharedPreferences.getString:" + mSharedPreferences.getString("hotspot", ""));
        mAddressList = mSharedPreferences.getString("hotspot", " ").split(",");
        updateBlacklist();
    }

    private void updateBlacklist() {
        mWifiTetherBlacklistPrefCategory.removeAll();
        System.out.println("mAddressList.length:" + mAddressList.length);
        if (mAddressList.length > 1) {
            for (int i = 0; i < mAddressList.length - 1; i++) {
                Preference pref = new Preference(getContext());
                String address = mAddressList[i];
                pref.setTitle(address);
                pref.setOnPreferenceClickListener(
                        preference -> {
                            System.out.println("connectedAddress: " + address);
                            showDisconnectDialog(address);
                            return true;
                        });
                mWifiTetherBlacklistPrefCategory.addPreference(pref);
            }
        }
    }

    private void showDisconnectDialog(String address) {
        final AlertDialog.Builder disconnectDialog = new AlertDialog.Builder(getContext());
        disconnectDialog.setTitle("Remove \"" + address + "\" from blacklist");
        disconnectDialog.setMessage("This device \"" + address + "\" will be able to connect to this hotspot");
        disconnectDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        String newAddressList = " ";
                        for (int i = mAddressList.length - 2; i >= 0; i--) {
                            if (!mAddressList[i].equals(address)) {
                                newAddressList = address + "," + newAddressList;
                            }
                        }
                        editor.putString("hotspot", newAddressList);
                        editor.commit();
                        mAddressList = newAddressList.split(",");
                        updateBlacklist();
                    }
                });
        disconnectDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        disconnectDialog.show();
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.wifi_tether_blacklist;
                    return Arrays.asList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    // Remove master switch as a result
                    keys.add(WifiMasterSwitchPreferenceController.KEY_TOGGLE_WIFI);
                    return keys;
                }
            };
}