package ru.temzit.wificontrol;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

/* loaded from: classes.dex */
public class ErrorsActivity extends PreferenceActivity {
    static long failures;

    @Override // android.preference.PreferenceActivity, android.app.Activity
    protected void onCreate(Bundle bundle) throws Resources.NotFoundException {
        super.onCreate(bundle);
        PreferenceScreen preferenceScreenCreatePreferenceScreen = getPreferenceManager().createPreferenceScreen(this);
        setPreferenceScreen(preferenceScreenCreatePreferenceScreen);
        String[] stringArray = getResources().getStringArray(temzit.wificontrol.R.array.failures_list);
        new Intent();
        failures = getIntent().getLongExtra("failures", 0L);
        for (int i = 0; i < 32; i++) {
            if (((failures >> i) & 1) == 1) {
                PreferenceCategory preferenceCategory = new PreferenceCategory(this);
                if (i < stringArray.length) {
                    preferenceCategory.setTitle(stringArray[i]);
                } else {
                    preferenceCategory.setTitle("Неизвестная авария. Код: " + failures + "(сдвиг " + i + ")");
                }
                preferenceScreenCreatePreferenceScreen.addPreference(preferenceCategory);
            }
        }
    }
}
