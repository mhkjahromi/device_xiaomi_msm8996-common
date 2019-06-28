/*
 * Copyright (C) 2016 The CyanogenMod Project
 *           (C) 2017-2018 The LineageOS Project
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

package com.cyanogenmod.settings.device;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import java.io.File;

import com.cyanogenmod.settings.device.utils.FileUtils;

public class Startup extends BroadcastReceiver {

    private static final String TAG = Startup.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_PRE_BOOT_COMPLETED.equals(action)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            VibratorStrengthPreference.restore(context);
            DisplayCalibration.restore(context);

            // Disable button settings if needed
            if (!hasButtonProcs()) {
                disableComponent(context, ButtonSettingsActivity.class.getName());
            } else {
                enableComponent(context, ButtonSettingsActivity.class.getName());

                // Restore nodes to saved preference values
                for (String pref : Constants.sButtonPrefKeys) {
                    String node, value;
                    if (Constants.sStringNodePreferenceMap.containsKey(pref)) {
                        node = Constants.sStringNodePreferenceMap.get(pref);
                        value = Utils.getPreferenceString(context, pref);
                    } else {
                        node = Constants.sBooleanNodePreferenceMap.get(pref);
                        value = Utils.isPreferenceEnabled(context, pref) ? "1" : "0";
                    }
                    if (!FileUtils.writeLine(node, value)) {
                        Log.w(TAG, "Write to node " + node +
                            " failed while restoring saved preference values");
                    }
                }

                // Send initial broadcasts
//                final boolean shouldEnablePocketMode =
//                        prefs.getBoolean(Constants.FP_WAKEUP_KEY, false) &&
//                        prefs.getBoolean(Constants.FP_POCKETMODE_KEY, false);
//                Utils.broadcastCustIntent(context, shouldEnablePocketMode);
                        prefs.getBoolean(Constants.FP_WAKEUP_KEY, false);
            }
        }
    }

    static boolean hasButtonProcs() {
        return new File(Constants.CYTTSP_BUTTON_SWAP_NODE).exists() ||
                new File(Constants.FP_HOME_KEY_NODE).exists() ||
                new File(Constants.FP_WAKEUP_NODE).exists() ||
                new File(Constants.TOUCHPANEL_BUTTON_SWAP_NODE).exists();
    }

    private void disableComponent(Context context, String component) {
        ComponentName name = new ComponentName(context, component);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(name,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    private void enableComponent(Context context, String component) {
        ComponentName name = new ComponentName(context, component);
        PackageManager pm = context.getPackageManager();
        if (pm.getComponentEnabledSetting(name)
                == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            pm.setComponentEnabledSetting(name,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }
}
