package ru.temzit.wificontrol;

import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.v4.view.InputDeviceCompat;

/* loaded from: classes.dex */
public class SettingsActivity extends PreferenceActivity {
    static final int CFG_LEN = 88;
    static String config = "001805000AE7002D00040201010005400A100000010A050028166363DAFF11B5150502B94413000003000000";
    int revision = 0;
    int version = 0;

    private static int hexToBin(char c) {
        if ('0' <= c && c <= '9') {
            return c - '0';
        }
        char c2 = 'A';
        if ('A' > c || c > 'F') {
            c2 = 'a';
            if ('a' > c || c > 'f') {
                return 0;
            }
        }
        return (c - c2) + 10;
    }

    protected int GetParamValue(String str, int i) {
        int i2 = i * 2;
        int iHexToBin = (hexToBin(config.charAt(i2)) << 4) | hexToBin(config.charAt(i2 + 1));
        return iHexToBin > 127 ? iHexToBin + InputDeviceCompat.SOURCE_ANY : iHexToBin;
    }

    protected void SetParamValue(int i, int i2) {
        String[] strArr = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        char cCharAt = strArr[(i2 >> 4) & 15].charAt(0);
        char cCharAt2 = strArr[i2 & 15].charAt(0);
        StringBuilder sb = new StringBuilder();
        int i3 = i * 2;
        sb.append(config.substring(0, i3));
        sb.append(cCharAt);
        sb.append(cCharAt2);
        sb.append(config.substring(i3 + 2));
        config = sb.toString();
    }

    public int checkParamList(ListPreference listPreference, int i) {
        CharSequence[] entryValues = listPreference.getEntryValues();
        if (i < 0) {
            i = entryValues.length;
        }
        if (i > entryValues.length - 1) {
            CharSequence[] entries = listPreference.getEntries();
            int i2 = i + 1;
            CharSequence[] charSequenceArr = new String[i2];
            CharSequence[] charSequenceArr2 = new String[i2];
            for (int i3 = 0; i3 < charSequenceArr.length; i3++) {
                if (i3 < entryValues.length) {
                    charSequenceArr[i3] = entryValues[i3];
                    charSequenceArr2[i3] = entries[i3];
                } else {
                    charSequenceArr[i3] = "Параметр " + Integer.toString(i3);
                    charSequenceArr2[i3] = "Параметр " + Integer.toString(i3);
                }
            }
            listPreference.setEntryValues(charSequenceArr);
            listPreference.setEntries(charSequenceArr2);
        }
        return i;
    }

    @Override // android.preference.PreferenceActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        int iGetParamValue;
        super.onCreate(bundle);
        PreferenceScreen preferenceScreenCreatePreferenceScreen = getPreferenceManager().createPreferenceScreen(this);
        setPreferenceScreen(preferenceScreenCreatePreferenceScreen);
        final Intent intent = new Intent();
        Intent intent2 = getIntent();
        config = intent2.getStringExtra("config");
        int intExtra = intent2.getIntExtra("ControllerRevision", -1);
        int length = 88 - config.length();
        if (length > 0) {
            for (int i = 0; i < length; i++) {
                config += "0";
            }
        }
        this.revision = (hexToBin(config.charAt(60)) << 12) | (hexToBin(config.charAt(61)) << 8) | (hexToBin(config.charAt(62)) << 4) | hexToBin(config.charAt(63));
        this.version = GetParamValue(config, 32);
        if (intExtra != -1) {
            this.revision = intExtra;
        }
        System.out.println("REVISION: " + this.revision);
        System.out.println("VERSION: " + this.version);
        final ListPreference listPreference = new ListPreference(this);
        listPreference.setSummary("Режим работы");
        listPreference.setEntries(temzit.wificontrol.R.array.mode);
        listPreference.setEntryValues(temzit.wificontrol.R.array.mode);
        int iCheckParamList = checkParamList(listPreference, GetParamValue(config, 0));
        listPreference.setValueIndex(iCheckParamList);
        listPreference.setTitle(listPreference.getEntries()[iCheckParamList]);
        preferenceScreenCreatePreferenceScreen.addPreference(listPreference);
        final ListPreference listPreference2 = new ListPreference(this);
        listPreference2.setTitle(listPreference2.getValue());
        listPreference2.setSummary("Температура в помещении");
        listPreference2.setEntries(temzit.wificontrol.R.array.entries);
        listPreference2.setEntryValues(temzit.wificontrol.R.array.entries);
        int iCheckParamList2 = checkParamList(listPreference2, GetParamValue(config, 1) - 16);
        listPreference2.setValueIndex(iCheckParamList2);
        listPreference2.setTitle(listPreference2.getEntries()[iCheckParamList2]);
        preferenceScreenCreatePreferenceScreen.addPreference(listPreference2);
        final ListPreference listPreference3 = new ListPreference(this);
        listPreference3.setSummary("Температура воды (обратки)");
        if (this.revision >= 332) {
            listPreference3.setEntries(temzit.wificontrol.R.array.water_new);
            listPreference3.setEntryValues(temzit.wificontrol.R.array.water_new);
            iGetParamValue = GetParamValue(config, 2) - 5;
        } else {
            listPreference3.setEntries(temzit.wificontrol.R.array.water);
            listPreference3.setEntryValues(temzit.wificontrol.R.array.water);
            iGetParamValue = GetParamValue(config, 2) - 16;
        }
        int iCheckParamList3 = checkParamList(listPreference3, iGetParamValue);
        listPreference3.setValueIndex(iCheckParamList3);
        listPreference3.setTitle(listPreference3.getEntries()[iCheckParamList3]);
        preferenceScreenCreatePreferenceScreen.addPreference(listPreference3);
        final ListPreference listPreference4 = new ListPreference(this);
        listPreference4.setSummary("Режим вспомогательного ТЭНа");
        listPreference4.setEntries(temzit.wificontrol.R.array.HeaterMode);
        listPreference4.setEntryValues(temzit.wificontrol.R.array.HeaterMode);
        int iCheckParamList4 = checkParamList(listPreference4, GetParamValue(config, 3) & 3);
        listPreference4.setValueIndex(iCheckParamList4);
        listPreference4.setTitle(listPreference4.getEntries()[iCheckParamList4]);
        preferenceScreenCreatePreferenceScreen.addPreference(listPreference4);
        final ListPreference listPreference5 = new ListPreference(this);
        listPreference5.setSummary("Внешний нагреватель");
        listPreference5.setEntries(temzit.wificontrol.R.array.ExtHeater);
        listPreference5.setEntryValues(temzit.wificontrol.R.array.ExtHeater);
        int iCheckParamList5 = checkParamList(listPreference5, GetParamValue(config, 8) & 15);
        listPreference5.setValueIndex(iCheckParamList5);
        listPreference5.setTitle(listPreference5.getEntries()[iCheckParamList5]);
        preferenceScreenCreatePreferenceScreen.addPreference(listPreference5);
        final ListPreference listPreference6 = new ListPreference(this);
        listPreference6.setSummary("Температура включения ТЭНа");
        listPreference6.setEntries(temzit.wificontrol.R.array.Theater);
        listPreference6.setEntryValues(temzit.wificontrol.R.array.Theater);
        int iCheckParamList6 = checkParamList(listPreference6, GetParamValue(config, 4) + 25);
        listPreference6.setValueIndex(iCheckParamList6);
        listPreference6.setTitle(listPreference6.getEntries()[iCheckParamList6]);
        preferenceScreenCreatePreferenceScreen.addPreference(listPreference6);
        final ListPreference listPreference7 = new ListPreference(this);
        listPreference7.setSummary("Температура выключения компрессора");
        listPreference7.setEntries(temzit.wificontrol.R.array.Tkkb);
        listPreference7.setEntryValues(temzit.wificontrol.R.array.Tkkb);
        int iCheckParamList7 = checkParamList(listPreference7, GetParamValue(config, 5) + 25);
        listPreference7.setValueIndex(iCheckParamList7);
        listPreference7.setTitle(listPreference7.getEntries()[iCheckParamList7]);
        preferenceScreenCreatePreferenceScreen.addPreference(listPreference7);
        final ListPreference listPreference8 = new ListPreference(this);
        listPreference8.setSummary("Ограничение мощности компрессора");
        listPreference8.setEntries(temzit.wificontrol.R.array.KKBlimit);
        listPreference8.setEntryValues(temzit.wificontrol.R.array.KKBlimit);
        int iCheckParamList8 = checkParamList(listPreference8, GetParamValue(config, 9));
        listPreference8.setValueIndex(iCheckParamList8);
        listPreference8.setTitle(listPreference8.getEntries()[iCheckParamList8]);
        preferenceScreenCreatePreferenceScreen.addPreference(listPreference8);
        final ListPreference listPreference9 = new ListPreference(this);
        listPreference9.setSummary("Коэффициент инерции дома");
        listPreference9.setEntries(temzit.wificontrol.R.array.Persistent);
        listPreference9.setEntryValues(temzit.wificontrol.R.array.Persistent);
        int iCheckParamList9 = checkParamList(listPreference9, (GetParamValue(config, 3) >> 4) & 15);
        listPreference9.setValueIndex(iCheckParamList9);
        listPreference9.setTitle(listPreference9.getEntries()[iCheckParamList9]);
        preferenceScreenCreatePreferenceScreen.addPreference(listPreference9);
        final ListPreference listPreference10 = new ListPreference(this);
        listPreference10.setSummary("Погодокомпенсация");
        listPreference10.setEntries(temzit.wificontrol.R.array.Whether);
        listPreference10.setEntryValues(temzit.wificontrol.R.array.Whether);
        int iCheckParamList10 = checkParamList(listPreference10, GetParamValue(config, 18));
        listPreference10.setValueIndex(iCheckParamList10);
        listPreference10.setTitle(listPreference10.getEntries()[iCheckParamList10]);
        preferenceScreenCreatePreferenceScreen.addPreference(listPreference10);
        PreferenceCategory preferenceCategory = new PreferenceCategory(this);
        preferenceCategory.setKey("categ1");
        preferenceCategory.setTitle("НАСТРОЙКИ ГВС");
        preferenceScreenCreatePreferenceScreen.addPreference(preferenceCategory);
        final ListPreference listPreference11 = new ListPreference(this);
        listPreference11.setSummary("Режим работы ГВС");
        listPreference11.setEntries(temzit.wificontrol.R.array.ModeGVS);
        listPreference11.setEntryValues(temzit.wificontrol.R.array.ModeGVS);
        int iCheckParamList11 = checkParamList(listPreference11, GetParamValue(config, 6) & 15);
        listPreference11.setValueIndex(iCheckParamList11);
        listPreference11.setTitle(listPreference11.getEntries()[iCheckParamList11]);
        preferenceCategory.addPreference(listPreference11);
        final ListPreference listPreference12 = new ListPreference(this);
        listPreference12.setSummary("Температура ГВС");
        listPreference12.setEntries(temzit.wificontrol.R.array.Tgvs);
        listPreference12.setEntryValues(temzit.wificontrol.R.array.Tgvs);
        int iCheckParamList12 = checkParamList(listPreference12, GetParamValue(config, 7) - 20);
        listPreference12.setValueIndex(iCheckParamList12);
        listPreference12.setTitle(listPreference12.getEntries()[iCheckParamList12]);
        preferenceCategory.addPreference(listPreference12);
        final ListPreference listPreference13 = new ListPreference(this);
        listPreference13.setSummary("Максимальная температура нагрева от ТН");
        listPreference13.setEntries(temzit.wificontrol.R.array.TgvsPump);
        listPreference13.setEntryValues(temzit.wificontrol.R.array.TgvsPump);
        int iCheckParamList13 = checkParamList(listPreference13, GetParamValue(config, 21));
        listPreference13.setValueIndex(iCheckParamList13);
        listPreference13.setTitle(listPreference13.getEntries()[iCheckParamList13]);
        preferenceCategory.addPreference(listPreference13);
        listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() { // from class: ru.temzit.wificontrol.SettingsActivity.1
            @Override // android.preference.Preference.OnPreferenceChangeListener
            public boolean onPreferenceChange(Preference preference, Object obj) {
                int iFindIndexOfValue = listPreference.findIndexOfValue(obj.toString());
                ListPreference listPreference14 = listPreference;
                listPreference14.setTitle(listPreference14.getEntries()[iFindIndexOfValue]);
                SettingsActivity.this.SetParamValue(0, iFindIndexOfValue);
                intent.putExtra("config", SettingsActivity.config);
                SettingsActivity.this.setResult(-1, intent);
                return true;
            }
        });
        listPreference2.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() { // from class: ru.temzit.wificontrol.SettingsActivity.2
            @Override // android.preference.Preference.OnPreferenceChangeListener
            public boolean onPreferenceChange(Preference preference, Object obj) {
                int iFindIndexOfValue = listPreference2.findIndexOfValue(obj.toString());
                ListPreference listPreference14 = listPreference2;
                listPreference14.setTitle(listPreference14.getEntries()[iFindIndexOfValue]);
                SettingsActivity.this.SetParamValue(1, iFindIndexOfValue + 16);
                intent.putExtra("config", SettingsActivity.config);
                SettingsActivity.this.setResult(-1, intent);
                return true;
            }
        });
        listPreference3.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() { // from class: ru.temzit.wificontrol.SettingsActivity.3
            @Override // android.preference.Preference.OnPreferenceChangeListener
            public boolean onPreferenceChange(Preference preference, Object obj) {
                int iFindIndexOfValue = listPreference3.findIndexOfValue(obj.toString());
                ListPreference listPreference14 = listPreference3;
                listPreference14.setTitle(listPreference14.getEntries()[iFindIndexOfValue]);
                if (SettingsActivity.this.revision >= 332) {
                    SettingsActivity.this.SetParamValue(2, iFindIndexOfValue + 5);
                } else {
                    SettingsActivity.this.SetParamValue(2, iFindIndexOfValue + 16);
                }
                intent.putExtra("config", SettingsActivity.config);
                SettingsActivity.this.setResult(-1, intent);
                return true;
            }
        });
        listPreference4.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() { // from class: ru.temzit.wificontrol.SettingsActivity.4
            @Override // android.preference.Preference.OnPreferenceChangeListener
            public boolean onPreferenceChange(Preference preference, Object obj) {
                int iFindIndexOfValue = listPreference4.findIndexOfValue(obj.toString());
                ListPreference listPreference14 = listPreference4;
                listPreference14.setTitle(listPreference14.getEntries()[iFindIndexOfValue]);
                SettingsActivity.this.SetParamValue(3, iFindIndexOfValue | (SettingsActivity.this.GetParamValue(SettingsActivity.config, 3) & 240));
                intent.putExtra("config", SettingsActivity.config);
                SettingsActivity.this.setResult(-1, intent);
                return true;
            }
        });
        listPreference5.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() { // from class: ru.temzit.wificontrol.SettingsActivity.5
            @Override // android.preference.Preference.OnPreferenceChangeListener
            public boolean onPreferenceChange(Preference preference, Object obj) {
                int iFindIndexOfValue = listPreference5.findIndexOfValue(obj.toString());
                ListPreference listPreference14 = listPreference5;
                listPreference14.setTitle(listPreference14.getEntries()[iFindIndexOfValue]);
                SettingsActivity.this.SetParamValue(8, iFindIndexOfValue);
                intent.putExtra("config", SettingsActivity.config);
                SettingsActivity.this.setResult(-1, intent);
                return true;
            }
        });
        listPreference6.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() { // from class: ru.temzit.wificontrol.SettingsActivity.6
            @Override // android.preference.Preference.OnPreferenceChangeListener
            public boolean onPreferenceChange(Preference preference, Object obj) {
                int iFindIndexOfValue = listPreference6.findIndexOfValue(obj.toString());
                ListPreference listPreference14 = listPreference6;
                listPreference14.setTitle(listPreference14.getEntries()[iFindIndexOfValue]);
                SettingsActivity.this.SetParamValue(4, iFindIndexOfValue - 25);
                intent.putExtra("config", SettingsActivity.config);
                SettingsActivity.this.setResult(-1, intent);
                return true;
            }
        });
        listPreference7.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() { // from class: ru.temzit.wificontrol.SettingsActivity.7
            @Override // android.preference.Preference.OnPreferenceChangeListener
            public boolean onPreferenceChange(Preference preference, Object obj) {
                int iFindIndexOfValue = listPreference7.findIndexOfValue(obj.toString());
                ListPreference listPreference14 = listPreference7;
                listPreference14.setTitle(listPreference14.getEntries()[iFindIndexOfValue]);
                SettingsActivity.this.SetParamValue(5, iFindIndexOfValue - 25);
                intent.putExtra("config", SettingsActivity.config);
                SettingsActivity.this.setResult(-1, intent);
                return true;
            }
        });
        listPreference8.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() { // from class: ru.temzit.wificontrol.SettingsActivity.8
            @Override // android.preference.Preference.OnPreferenceChangeListener
            public boolean onPreferenceChange(Preference preference, Object obj) {
                int iFindIndexOfValue = listPreference8.findIndexOfValue(obj.toString());
                ListPreference listPreference14 = listPreference8;
                listPreference14.setTitle(listPreference14.getEntries()[iFindIndexOfValue]);
                SettingsActivity.this.SetParamValue(9, iFindIndexOfValue);
                intent.putExtra("config", SettingsActivity.config);
                SettingsActivity.this.setResult(-1, intent);
                return true;
            }
        });
        listPreference9.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() { // from class: ru.temzit.wificontrol.SettingsActivity.9
            @Override // android.preference.Preference.OnPreferenceChangeListener
            public boolean onPreferenceChange(Preference preference, Object obj) {
                int iFindIndexOfValue = listPreference9.findIndexOfValue(obj.toString());
                ListPreference listPreference14 = listPreference9;
                listPreference14.setTitle(listPreference14.getEntries()[iFindIndexOfValue]);
                SettingsActivity.this.SetParamValue(3, (iFindIndexOfValue << 4) | (SettingsActivity.this.GetParamValue(SettingsActivity.config, 3) & 3));
                intent.putExtra("config", SettingsActivity.config);
                SettingsActivity.this.setResult(-1, intent);
                return true;
            }
        });
        listPreference10.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() { // from class: ru.temzit.wificontrol.SettingsActivity.10
            @Override // android.preference.Preference.OnPreferenceChangeListener
            public boolean onPreferenceChange(Preference preference, Object obj) {
                int iFindIndexOfValue = listPreference10.findIndexOfValue(obj.toString());
                ListPreference listPreference14 = listPreference10;
                listPreference14.setTitle(listPreference14.getEntries()[iFindIndexOfValue]);
                SettingsActivity.this.SetParamValue(18, iFindIndexOfValue);
                intent.putExtra("config", SettingsActivity.config);
                SettingsActivity.this.setResult(-1, intent);
                return true;
            }
        });
        listPreference11.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() { // from class: ru.temzit.wificontrol.SettingsActivity.11
            @Override // android.preference.Preference.OnPreferenceChangeListener
            public boolean onPreferenceChange(Preference preference, Object obj) {
                int iFindIndexOfValue = listPreference11.findIndexOfValue(obj.toString());
                ListPreference listPreference14 = listPreference11;
                listPreference14.setTitle(listPreference14.getEntries()[iFindIndexOfValue]);
                SettingsActivity.this.SetParamValue(6, iFindIndexOfValue | (SettingsActivity.this.GetParamValue(SettingsActivity.config, 6) & 240));
                intent.putExtra("config", SettingsActivity.config);
                SettingsActivity.this.setResult(-1, intent);
                return true;
            }
        });
        listPreference12.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() { // from class: ru.temzit.wificontrol.SettingsActivity.12
            @Override // android.preference.Preference.OnPreferenceChangeListener
            public boolean onPreferenceChange(Preference preference, Object obj) {
                int iFindIndexOfValue = listPreference12.findIndexOfValue(obj.toString());
                ListPreference listPreference14 = listPreference12;
                listPreference14.setTitle(listPreference14.getEntries()[iFindIndexOfValue]);
                SettingsActivity.this.SetParamValue(7, iFindIndexOfValue + 20);
                intent.putExtra("config", SettingsActivity.config);
                SettingsActivity.this.setResult(-1, intent);
                return true;
            }
        });
        listPreference13.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() { // from class: ru.temzit.wificontrol.SettingsActivity.13
            @Override // android.preference.Preference.OnPreferenceChangeListener
            public boolean onPreferenceChange(Preference preference, Object obj) {
                int iFindIndexOfValue = listPreference13.findIndexOfValue(obj.toString());
                ListPreference listPreference14 = listPreference13;
                listPreference14.setTitle(listPreference14.getEntries()[iFindIndexOfValue]);
                SettingsActivity.this.SetParamValue(21, iFindIndexOfValue);
                intent.putExtra("config", SettingsActivity.config);
                SettingsActivity.this.setResult(-1, intent);
                return true;
            }
        });
    }
}
