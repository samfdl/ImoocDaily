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

import android.app.settings.SettingsEnums;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.UserManager;
import android.provider.SearchIndexableResource;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.VisibleForTesting;

import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.dashboard.RestrictedDashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.widget.SwitchBar;
import com.android.settings.widget.SwitchBarController;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.net.ConnectivityManager.ACTION_TETHER_STATE_CHANGED;
import static android.net.wifi.WifiManager.WIFI_AP_STATE_CHANGED_ACTION;
import static android.net.wifi.WifiManager.WIFI_COUNTRY_CODE_CHANGED_ACTION;

@SearchIndexable
public class WifiHotspotBlocklist extends RestrictedDashboardFragment
        implements WifiTetherBasePreferenceController.OnTetherConfigUpdateListener {

    private static final String TAG = "WifiHotspotBlocklist";
    private static final String KEY_WIFI_HOTSPOT_BLOCKLIST_SCREEN = "wifi_hotspot_blocklist_screen";

    private boolean mUnavailable;

    public WifiHotspotBlocklist() {
        super(UserManager.DISALLOW_CONFIG_TETHERING);
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.WIFI_TETHER_SETTINGS;
    }

    @Override
    protected String getLogTag() {
        return "WifiHotspotBlocklist";
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setIfOnlyAvailableForAdmins(true);
        if (isUiRestricted()) {
            mUnavailable = true;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mUnavailable) {
            return;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mUnavailable) {
            if (!isUiRestrictedByOnlyAdmin()) {
                getEmptyTextView().setText(R.string.tethering_settings_not_available);
            }
            getPreferenceScreen().removeAll();
            return;
        }
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.wifi_hotspot_blocklist;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, this::onTetherConfigUpdated);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context,
            WifiTetherBasePreferenceController.OnTetherConfigUpdateListener listener) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new WifiTetherConnectedDevicesPreferenceController(context, listener));

        return controllers;
    }

    @Override
    public void onTetherConfigUpdated() {
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.wifi_hotspot_blocklist;
                    return Arrays.asList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    final List<String> keys = super.getNonIndexableKeys(context);

                    // Remove duplicate
                    keys.add(KEY_WIFI_HOTSPOT_BLOCKLIST_SCREEN);
                    return keys;
                }

                @Override
                public List<AbstractPreferenceController> createPreferenceControllers(
                        Context context) {
                    return buildPreferenceControllers(context, null /* listener */);
                }
            };
}
