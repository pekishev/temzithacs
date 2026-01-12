package ru.temzit.wificontrol;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.v4.view.InputDeviceCompat;

/* loaded from: classes.dex */
public class ScheduleActivity extends PreferenceActivity {
    static String config = "01101E00F6E7052300000A0500010613590F030000000F0000055A00";

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

    public class myListPreference extends ListPreference {
        public int ConfigOffset;

        public myListPreference(Context context, int i) {
            super(context);
            this.ConfigOffset = i;
        }

        public void SetConfigOffset(int i) {
            this.ConfigOffset = i;
        }

        int GetConfigOffset() {
            return this.ConfigOffset;
        }
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

    @Override // android.preference.PreferenceActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        PreferenceScreen preferenceScreenCreatePreferenceScreen = getPreferenceManager().createPreferenceScreen(this);
        setPreferenceScreen(preferenceScreenCreatePreferenceScreen);
        Intent intent = new Intent();
        config = getIntent().getStringExtra("config");
        ListPreference[] listPreferenceArr = new ListPreference[4];
        ListPreference[] listPreferenceArr2 = new ListPreference[4];
        ListPreference[] listPreferenceArr3 = new ListPreference[4];
        ListPreference[] listPreferenceArr4 = new ListPreference[4];
        ListPreference[] listPreferenceArr5 = new ListPreference[4];
        ListPreference[] listPreferenceArr6 = new ListPreference[4];
        ListPreference[] listPreferenceArr7 = new ListPreference[4];
        ListPreference[] listPreferenceArr8 = new ListPreference[4];
        ListPreference[] listPreferenceArr9 = new ListPreference[4];
        int i = 0;
        for (int i2 = 4; i < i2; i2 = 4) {
            PreferenceScreen preferenceScreenCreatePreferenceScreen2 = getPreferenceManager().createPreferenceScreen(this);
            preferenceScreenCreatePreferenceScreen2.setSummary("настройки");
            StringBuilder sb = new StringBuilder();
            sb.append("Расписание №");
            int i3 = i + 1;
            sb.append(i3);
            preferenceScreenCreatePreferenceScreen2.setTitle(sb.toString());
            preferenceScreenCreatePreferenceScreen.addPreference(preferenceScreenCreatePreferenceScreen2);
            PreferenceCategory preferenceCategory = new PreferenceCategory(this);
            PreferenceScreen preferenceScreen = preferenceScreenCreatePreferenceScreen;
            StringBuilder sb2 = new StringBuilder();
            final Intent intent2 = intent;
            sb2.append("Настройки расписания №");
            sb2.append(i3);
            preferenceCategory.setTitle(sb2.toString());
            preferenceCategory.setSummary("Description of category 1");
            preferenceScreenCreatePreferenceScreen2.addPreference(preferenceCategory);
            int i4 = i * 10;
            listPreferenceArr[i] = new myListPreference(this, i4);
            listPreferenceArr[i].setTitle("Режим работы расписания");
            listPreferenceArr[i].setEntries(temzit.wificontrol.R.array.sch_mode);
            listPreferenceArr[i].setEntryValues(temzit.wificontrol.R.array.sch_mode);
            int iGetParamValue = GetParamValue(config, i4);
            listPreferenceArr[i].setValueIndex(iGetParamValue);
            listPreferenceArr[i].setSummary(listPreferenceArr[i].getEntries()[iGetParamValue]);
            ListPreference listPreference = listPreferenceArr[i];
            StringBuilder sb3 = new StringBuilder();
            ListPreference[] listPreferenceArr10 = listPreferenceArr9;
            sb3.append("Режим работы расписания № ");
            sb3.append(i3);
            listPreference.setDialogTitle(sb3.toString());
            preferenceCategory.addPreference(listPreferenceArr[i]);
            int i5 = i4 + 1;
            listPreferenceArr2[i] = new myListPreference(this, i5);
            listPreferenceArr2[i].setTitle("Время начала");
            listPreferenceArr2[i].setEntries(temzit.wificontrol.R.array.sch_time);
            listPreferenceArr2[i].setEntryValues(temzit.wificontrol.R.array.sch_time);
            int iGetParamValue2 = GetParamValue(config, i5);
            listPreferenceArr2[i].setValueIndex(iGetParamValue2);
            listPreferenceArr2[i].setSummary(listPreferenceArr2[i].getEntries()[iGetParamValue2]);
            listPreferenceArr2[i].setDialogTitle("Время начала расписания № " + i3);
            preferenceCategory.addPreference(listPreferenceArr2[i]);
            int i6 = i4 + 2;
            listPreferenceArr3[i] = new myListPreference(this, i6);
            listPreferenceArr3[i].setTitle("Время окончания");
            listPreferenceArr3[i].setEntries(temzit.wificontrol.R.array.sch_time);
            listPreferenceArr3[i].setEntryValues(temzit.wificontrol.R.array.sch_time);
            int iGetParamValue3 = GetParamValue(config, i6);
            listPreferenceArr3[i].setValueIndex(iGetParamValue3);
            listPreferenceArr3[i].setSummary(listPreferenceArr3[i].getEntries()[iGetParamValue3]);
            listPreferenceArr3[i].setDialogTitle("Время окончания расписания № " + i3);
            preferenceCategory.addPreference(listPreferenceArr3[i]);
            int i7 = i4 + 3;
            listPreferenceArr4[i] = new myListPreference(this, i7);
            listPreferenceArr4[i].setTitle("Температура в помещении");
            listPreferenceArr4[i].setEntries(temzit.wificontrol.R.array.entries);
            listPreferenceArr4[i].setEntryValues(temzit.wificontrol.R.array.entries);
            int iGetParamValue4 = GetParamValue(config, i7) - 16;
            if (iGetParamValue4 < 0) {
                iGetParamValue4 = 0;
            }
            listPreferenceArr4[i].setValueIndex(iGetParamValue4);
            listPreferenceArr4[i].setSummary(listPreferenceArr4[i].getEntries()[iGetParamValue4]);
            listPreferenceArr4[i].setDialogTitle("Температура в помещении расписание № " + i3);
            preferenceCategory.addPreference(listPreferenceArr4[i]);
            int i8 = i4 + 4;
            listPreferenceArr5[i] = new myListPreference(this, i8);
            listPreferenceArr5[i].setTitle("Температуры воды (обратки)");
            listPreferenceArr5[i].setEntries(temzit.wificontrol.R.array.water);
            listPreferenceArr5[i].setEntryValues(temzit.wificontrol.R.array.water);
            int iGetParamValue5 = GetParamValue(config, i8) - 16;
            if (iGetParamValue5 < 0) {
                iGetParamValue5 = 0;
            }
            listPreferenceArr5[i].setValueIndex(iGetParamValue5);
            listPreferenceArr5[i].setSummary(listPreferenceArr5[i].getEntries()[iGetParamValue5]);
            listPreferenceArr5[i].setDialogTitle("Температуры воды в расписании № " + i3);
            preferenceCategory.addPreference(listPreferenceArr5[i]);
            int i9 = i4 + 7;
            listPreferenceArr6[i] = new myListPreference(this, i9);
            listPreferenceArr6[i].setTitle("Режим использования ТЭНа");
            listPreferenceArr6[i].setEntries(temzit.wificontrol.R.array.HeaterMode);
            listPreferenceArr6[i].setEntryValues(temzit.wificontrol.R.array.HeaterMode);
            int iGetParamValue6 = GetParamValue(config, i9);
            listPreferenceArr6[i].setValueIndex(iGetParamValue6);
            listPreferenceArr6[i].setSummary(listPreferenceArr6[i].getEntries()[iGetParamValue6]);
            listPreferenceArr6[i].setDialogTitle("Режим ТЭНа в расписанив № " + i3);
            preferenceCategory.addPreference(listPreferenceArr6[i]);
            int i10 = i4 + 6;
            listPreferenceArr7[i] = new myListPreference(this, i10);
            listPreferenceArr7[i].setTitle("Ограничения компрессора");
            listPreferenceArr7[i].setEntries(temzit.wificontrol.R.array.KKBlimit);
            listPreferenceArr7[i].setEntryValues(temzit.wificontrol.R.array.KKBlimit);
            int iGetParamValue7 = GetParamValue(config, i10);
            listPreferenceArr7[i].setValueIndex(iGetParamValue7);
            listPreferenceArr7[i].setSummary(listPreferenceArr7[i].getEntries()[iGetParamValue7]);
            listPreferenceArr7[i].setDialogTitle("Ограничения ККБ в расписании № " + i3);
            preferenceCategory.addPreference(listPreferenceArr7[i]);
            int i11 = i4 + 8;
            listPreferenceArr8[i] = new myListPreference(this, i11);
            listPreferenceArr8[i].setTitle("Режим ГВС");
            listPreferenceArr8[i].setEntries(temzit.wificontrol.R.array.ModeGVSSchedul);
            listPreferenceArr8[i].setEntryValues(temzit.wificontrol.R.array.ModeGVSSchedul);
            int iGetParamValue8 = GetParamValue(config, i11);
            listPreferenceArr8[i].setValueIndex(iGetParamValue8);
            listPreferenceArr8[i].setSummary(listPreferenceArr8[i].getEntries()[iGetParamValue8]);
            listPreferenceArr8[i].setDialogTitle("Режим ГВС расписания № " + i3);
            preferenceCategory.addPreference(listPreferenceArr8[i]);
            int i12 = i4 + 5;
            listPreferenceArr10[i] = new myListPreference(this, i12);
            listPreferenceArr10[i].setTitle("Температура ГВС");
            listPreferenceArr10[i].setEntries(temzit.wificontrol.R.array.Tgvs);
            listPreferenceArr10[i].setEntryValues(temzit.wificontrol.R.array.Tgvs);
            int iGetParamValue9 = GetParamValue(config, i12) - 20;
            if (iGetParamValue9 < 0) {
                iGetParamValue9 = 0;
            }
            listPreferenceArr10[i].setValueIndex(iGetParamValue9);
            listPreferenceArr10[i].setSummary(listPreferenceArr10[i].getEntries()[iGetParamValue9]);
            listPreferenceArr10[i].setDialogTitle("Температура ГВС расписания № " + i3);
            preferenceCategory.addPreference(listPreferenceArr10[i]);
            listPreferenceArr[i].setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() { // from class: ru.temzit.wificontrol.ScheduleActivity.1
                @Override // android.preference.Preference.OnPreferenceChangeListener
                public boolean onPreferenceChange(Preference preference, Object obj) {
                    myListPreference mylistpreference = (myListPreference) preference;
                    int iFindIndexOfValue = mylistpreference.findIndexOfValue(obj.toString());
                    mylistpreference.setSummary(mylistpreference.getEntries()[iFindIndexOfValue]);
                    ScheduleActivity.this.SetParamValue(mylistpreference.ConfigOffset, iFindIndexOfValue);
                    intent2.putExtra("config", ScheduleActivity.config);
                    ScheduleActivity.this.setResult(-1, intent2);
                    return true;
                }
            });
            listPreferenceArr2[i].setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() { // from class: ru.temzit.wificontrol.ScheduleActivity.2
                @Override // android.preference.Preference.OnPreferenceChangeListener
                public boolean onPreferenceChange(Preference preference, Object obj) {
                    myListPreference mylistpreference = (myListPreference) preference;
                    int iFindIndexOfValue = mylistpreference.findIndexOfValue(obj.toString());
                    mylistpreference.setSummary(mylistpreference.getEntries()[iFindIndexOfValue]);
                    ScheduleActivity.this.SetParamValue(mylistpreference.ConfigOffset, iFindIndexOfValue);
                    intent2.putExtra("config", ScheduleActivity.config);
                    ScheduleActivity.this.setResult(-1, intent2);
                    return true;
                }
            });
            listPreferenceArr3[i].setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() { // from class: ru.temzit.wificontrol.ScheduleActivity.3
                @Override // android.preference.Preference.OnPreferenceChangeListener
                public boolean onPreferenceChange(Preference preference, Object obj) {
                    myListPreference mylistpreference = (myListPreference) preference;
                    int iFindIndexOfValue = mylistpreference.findIndexOfValue(obj.toString());
                    mylistpreference.setSummary(mylistpreference.getEntries()[iFindIndexOfValue]);
                    ScheduleActivity.this.SetParamValue(mylistpreference.ConfigOffset, iFindIndexOfValue);
                    intent2.putExtra("config", ScheduleActivity.config);
                    ScheduleActivity.this.setResult(-1, intent2);
                    return true;
                }
            });
            listPreferenceArr4[i].setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() { // from class: ru.temzit.wificontrol.ScheduleActivity.4
                @Override // android.preference.Preference.OnPreferenceChangeListener
                public boolean onPreferenceChange(Preference preference, Object obj) {
                    myListPreference mylistpreference = (myListPreference) preference;
                    int iFindIndexOfValue = mylistpreference.findIndexOfValue(obj.toString());
                    mylistpreference.setSummary(mylistpreference.getEntries()[iFindIndexOfValue]);
                    ScheduleActivity.this.SetParamValue(mylistpreference.ConfigOffset, iFindIndexOfValue + 16);
                    intent2.putExtra("config", ScheduleActivity.config);
                    ScheduleActivity.this.setResult(-1, intent2);
                    return true;
                }
            });
            listPreferenceArr5[i].setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() { // from class: ru.temzit.wificontrol.ScheduleActivity.5
                @Override // android.preference.Preference.OnPreferenceChangeListener
                public boolean onPreferenceChange(Preference preference, Object obj) {
                    myListPreference mylistpreference = (myListPreference) preference;
                    int iFindIndexOfValue = mylistpreference.findIndexOfValue(obj.toString());
                    mylistpreference.setSummary(mylistpreference.getEntries()[iFindIndexOfValue]);
                    ScheduleActivity.this.SetParamValue(mylistpreference.ConfigOffset, iFindIndexOfValue + 16);
                    intent2.putExtra("config", ScheduleActivity.config);
                    ScheduleActivity.this.setResult(-1, intent2);
                    return true;
                }
            });
            listPreferenceArr6[i].setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() { // from class: ru.temzit.wificontrol.ScheduleActivity.6
                @Override // android.preference.Preference.OnPreferenceChangeListener
                public boolean onPreferenceChange(Preference preference, Object obj) {
                    myListPreference mylistpreference = (myListPreference) preference;
                    int iFindIndexOfValue = mylistpreference.findIndexOfValue(obj.toString());
                    mylistpreference.setSummary(mylistpreference.getEntries()[iFindIndexOfValue]);
                    ScheduleActivity.this.SetParamValue(mylistpreference.ConfigOffset, iFindIndexOfValue);
                    intent2.putExtra("config", ScheduleActivity.config);
                    ScheduleActivity.this.setResult(-1, intent2);
                    return true;
                }
            });
            listPreferenceArr7[i].setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() { // from class: ru.temzit.wificontrol.ScheduleActivity.7
                @Override // android.preference.Preference.OnPreferenceChangeListener
                public boolean onPreferenceChange(Preference preference, Object obj) {
                    myListPreference mylistpreference = (myListPreference) preference;
                    int iFindIndexOfValue = mylistpreference.findIndexOfValue(obj.toString());
                    mylistpreference.setSummary(mylistpreference.getEntries()[iFindIndexOfValue]);
                    ScheduleActivity.this.SetParamValue(mylistpreference.ConfigOffset, iFindIndexOfValue);
                    intent2.putExtra("config", ScheduleActivity.config);
                    ScheduleActivity.this.setResult(-1, intent2);
                    return true;
                }
            });
            listPreferenceArr8[i].setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() { // from class: ru.temzit.wificontrol.ScheduleActivity.8
                @Override // android.preference.Preference.OnPreferenceChangeListener
                public boolean onPreferenceChange(Preference preference, Object obj) {
                    myListPreference mylistpreference = (myListPreference) preference;
                    int iFindIndexOfValue = mylistpreference.findIndexOfValue(obj.toString());
                    mylistpreference.setSummary(mylistpreference.getEntries()[iFindIndexOfValue]);
                    ScheduleActivity.this.SetParamValue(mylistpreference.ConfigOffset, iFindIndexOfValue);
                    intent2.putExtra("config", ScheduleActivity.config);
                    ScheduleActivity.this.setResult(-1, intent2);
                    return true;
                }
            });
            listPreferenceArr10[i].setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() { // from class: ru.temzit.wificontrol.ScheduleActivity.9
                @Override // android.preference.Preference.OnPreferenceChangeListener
                public boolean onPreferenceChange(Preference preference, Object obj) {
                    myListPreference mylistpreference = (myListPreference) preference;
                    int iFindIndexOfValue = mylistpreference.findIndexOfValue(obj.toString());
                    mylistpreference.setSummary(mylistpreference.getEntries()[iFindIndexOfValue]);
                    ScheduleActivity.this.SetParamValue(mylistpreference.ConfigOffset, iFindIndexOfValue + 20);
                    intent2.putExtra("config", ScheduleActivity.config);
                    ScheduleActivity.this.setResult(-1, intent2);
                    return true;
                }
            });
            intent = intent2;
            i = i3;
            preferenceScreenCreatePreferenceScreen = preferenceScreen;
            listPreferenceArr9 = listPreferenceArr10;
        }
    }
}
